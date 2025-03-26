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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class KickCommand extends AbstractCommand {

    public KickCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext) {
        super(repositoryBundle, commService, applicationContext);
    }

    @Override
    public Question execute(Question question, WebSocketContext webSocketContext, List<String> tokens, Input input, Output output) {

        MudCharacter ch = getCurrentCharacter(webSocketContext, output);

        if (tokens.size() < 2) {
            output.append("[default]Whom would you like to kick?");
            return question;
        }
        Optional<MudCharacter> targetOptional = findWorldCharacter(ch, tokens.get(1));

        if (targetOptional.isEmpty() || targetOptional.get().getPlayer() == null) {
            output.append("[red]Can't find that player.");
            return question;
        }

        MudCharacter target = targetOptional.get();

        String principal = target.getCreatedBy();

        getCommService().reloadUser(principal);

        LOGGER.info("{} has been kicked.", ch.getCharacter().getName());

        getCommService().sendToAll(webSocketContext,
            new Output("[yellow]%s has been kicked!", ch.getCharacter().getName()), ch);

        return question;
    }
}
