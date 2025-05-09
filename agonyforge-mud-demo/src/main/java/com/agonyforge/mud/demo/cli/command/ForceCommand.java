package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.CommandReference;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.repository.CommandRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ForceCommand extends AbstractCommand {

    private final CommandRepository commandRepository;

    @Autowired
    public ForceCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext, CommandRepository commandRepository) {
        super(repositoryBundle, commService, applicationContext);
        this.commandRepository = commandRepository;
    }

    // FORCE <user> <command> ...
    @Override
    public Question execute(Question question, WebSocketContext webSocketContext, List<String> tokens, Input input, Output output) {
        MudCharacter ch = getCurrentCharacter(webSocketContext, output);

        if (tokens.size() < 2) {
            output.append("[default]Whom would you like to force to do something?");
            return question;
        }

        Optional<MudCharacter> targetOptional = findWorldCharacter(ch, tokens.get(1));

        if (targetOptional.isEmpty() || targetOptional.get().getPlayer() == null) {
            output.append("[red]Can't find that player.");
            return question;
        }

        MudCharacter target = targetOptional.get();

        if (tokens.size() < 3) {
            output.append("[default]What would you like to force them to do?");
            return question;
        }


        String fullCommand = Command.stripFirstWords(input.getInput(), 2);
        String commandName = fullCommand.split(" ")[0];

        Optional<CommandReference> commandReferenceOptional = commandRepository.findByNameIgnoreCase(commandName);

        if (commandReferenceOptional.isEmpty()) {
            output.append("[red]Can't find that command.");
            return question;
        }

        CommandReference commandReference = commandReferenceOptional.get();

        if (!commandReference.isCanBeForced()) {
            output.append("[red]Can't force %s to %s", target.getCharacter().getName(), commandName);
            return question;
        }

        if (target.isFrozen() && !commandReference.isCanExecuteWhileFrozen()) {
            output.append("[red]Can't execute this command, %s is frozen.", target.getCharacter().getName());
            return question;
        }

        // replaces current user with the one who is forced to do something
        webSocketContext.getAttributes().put("force_user", target.getCharacter().getId());

        Command command;
        try {
            command = this.getApplicationContext().getBean(commandReference.getBeanName(), Command.class);
        }catch (NoSuchBeanDefinitionException e) {
            output.append("[red]Command not found");
            return question;
        }



        ArrayList<String> forceCommandTokens = new ArrayList<>();

        for (String token : fullCommand.split(" ")) {
            forceCommandTokens.add(token.toUpperCase());
        }

        Output forcedUserOutput = new Output();

        command.execute(question, webSocketContext, List.of(forceCommandTokens.toArray(new String[0])) , new Input(fullCommand), forcedUserOutput);

        String forceDescription = String.format("[yellow]%s forced you to: %s\n", ch.getCharacter().getName(), fullCommand);
        Output forcedUserOutputToTarget = new Output(new Output(forceDescription), new Output(" "), forcedUserOutput);
        getCommService().sendTo(target, forcedUserOutputToTarget);

        forcedUserOutput = forcedUserOutput.prepend(String.format("[yellow]%s beheld the following:", target.getCharacter().getName()));
        forcedUserOutput = forcedUserOutput.prepend(" ");
        getCommService().sendTo(ch, forcedUserOutput);

        webSocketContext.getAttributes().remove("force_user");

        return question;
    }
}
