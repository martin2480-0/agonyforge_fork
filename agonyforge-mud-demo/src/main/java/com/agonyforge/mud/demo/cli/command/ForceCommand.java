package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ForceCommand extends AbstractCommand {


    @Autowired
    public ForceCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext) {
        super(repositoryBundle, commService, applicationContext);
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
        String command = fullCommand.split(" ")[0];

        output.append("[yellow]You forced %s to: %s",target.getCharacter().getName(), fullCommand);
        getCommService().sendTo(target, new Output("[yellow]%s forced you to: %s", ch.getCharacter().getName(), fullCommand));

        webSocketContext.getAttributes().put("force", true);
        webSocketContext.getAttributes().put("force_user", target.getCharacter().getId());


        switch (command.toLowerCase()) {
            case "down", "south" -> {}
            case "up", "north" -> {}
            case "east" -> {}
            case "west" -> {}
            case "northeast", "ne" -> {}
            case "northwest", "nw" -> {}
            case "southwest", "sw" -> {}
            case "southeast", "se" -> {}
            case "look" -> {}
            case "emote" -> {}
            case "whisper" -> {}
            case "give" -> {}
            case "get" -> {}
            case "gossip" -> {}
            case "say" -> {}
            case "shout" -> {}
            case "drop" -> {}
            case "wear" -> {}
            case "tell" -> {}
            case "remove" -> {}
        }

        webSocketContext.getAttributes().remove("force_user");
        webSocketContext.getAttributes().remove("force");

    return question;
    }
}
