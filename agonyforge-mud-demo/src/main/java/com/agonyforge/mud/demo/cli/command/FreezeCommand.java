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
public class FreezeCommand extends AbstractCommand{

    @Autowired
    public FreezeCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext) {
        super(repositoryBundle, commService, applicationContext);
    }

    @Override
    public Question execute(Question question, WebSocketContext webSocketContext, List<String> tokens, Input input, Output output) {
        MudCharacter ch = getCurrentCharacter(webSocketContext, output);

        if (tokens.size() < 2) {
            output.append("[default]Whom would you like to freeze/unfreeze?");
            return question;
        }
        Optional<MudCharacter> targetOptional = findWorldCharacter(ch, tokens.get(1));

        if (targetOptional.isEmpty() || targetOptional.get().getPlayer() == null) {
            output.append("[red]Can't find that player.");
            return question;
        }

        MudCharacter target = targetOptional.get();

        boolean newState = !target.isFrozen();

        target.setFrozen(newState);

        if (newState){
            output.append("[default]%s has been frozen.", target.getCharacter().getName());
            getCommService().sendTo(target, new Output("[yellow]You have been frozen.", ch.getCharacter().getName()));
        }else {
            output.append("[default]%s has been unfrozen.", target.getCharacter().getName());
            getCommService().sendTo(target, new Output("[yellow]You have been unfrozen.", ch.getCharacter().getName()));
        }

        return question;
    }
}
