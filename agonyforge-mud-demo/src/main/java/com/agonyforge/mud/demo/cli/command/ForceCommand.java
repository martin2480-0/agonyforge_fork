package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.CommandReference;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.repository.CommandForceRepository;
import com.agonyforge.mud.demo.model.repository.CommandRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class ForceCommand extends AbstractCommand {

    private final CommandForceRepository commandForceRepository;
    private final CommandRepository commandRepository;

    public static final Set<String> SUPPORTED_COMMANDS = Set.of(
        "down", "south", "up", "north", "east", "west",
        "northeast", "ne", "northwest", "nw", "southwest", "sw", "southeast", "se",
        "look", "emote", "whisper", "give", "get", "gossip",
        "say", "shout", "drop", "wear", "tell", "remove"
    );

    public static final Set<String> MOVEMENT_COMMANDS = Set.of(
        "down", "south", "up", "north", "east", "west",
        "northeast", "ne", "northwest", "nw", "southwest", "sw", "southeast", "se"
    );

    @Autowired
    public ForceCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext, CommandForceRepository commandForceRepository, CommandRepository commandRepository) {
        super(repositoryBundle, commService, applicationContext);
        this.commandForceRepository = commandForceRepository;
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

        if (tokens.size() < 3) {
            output.append("[default]What would you like to force them to do?");
            return question;
        }

        Optional<MudCharacter> targetOptional = findWorldCharacter(ch, tokens.get(1));

        if (targetOptional.isEmpty() || targetOptional.get().getPlayer() == null) {
            output.append("[red]Can't find that player.");
            return question;
        }

        MudCharacter target = targetOptional.get();

        String fullCommand = Command.stripFirstWords(input.getInput(), 2);
        String commandName = fullCommand.split(" ")[0];

        Optional<CommandReference> commandReferenceOptional = commandRepository.findByNameIgnoreCase(commandName);
        if (commandReferenceOptional.isEmpty()) {
            output.append("[red]Can't find that command.");
            return question;
        }

        CommandReference commandReference = commandReferenceOptional.get();

        if (!commandReference.getCommandForce().isForcible()) {
            output.append("[red]Can't force %s to do: %s", target.getCharacter().getName(), commandName);
            return question;
        }

        output.append("[yellow]You forced %s to: %s",target.getCharacter().getName(), fullCommand);
        getCommService().sendTo(target, new Output("[yellow]%s forced you to: %s", ch.getCharacter().getName(), fullCommand));

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

        command.execute(question, webSocketContext,List.of(forceCommandTokens.toArray(new String[0])) , new Input(fullCommand), new Output());

        webSocketContext.getAttributes().remove("force_user");

        return question;
    }
}
