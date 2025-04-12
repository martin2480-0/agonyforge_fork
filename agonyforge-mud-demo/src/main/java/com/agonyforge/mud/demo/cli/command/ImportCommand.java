package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.impl.ReloadedUser;
import com.agonyforge.mud.demo.model.impl.User;
import com.agonyforge.mud.demo.model.repository.ReloadedUsersRepository;
import com.agonyforge.mud.demo.model.repository.UserRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ImportCommand extends AbstractCommand {

    private final ReloadedUsersRepository reloadedUsersRepository;
    private final UserRepository userRepository;

    @Autowired
    public ImportCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext, ReloadedUsersRepository reloadedUsersRepository, UserRepository userRepository) {
        super(repositoryBundle, commService, applicationContext);
        this.reloadedUsersRepository = reloadedUsersRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Question execute(Question question, WebSocketContext webSocketContext, List<String> tokens, Input input, Output output) {
        MudCharacter ch = getCurrentCharacter(webSocketContext, output);

        if (tokens.size() < 2) {
            output.append("[default]What would you like to import?");
            return question;
        }

        String type = tokens.get(1).toLowerCase();

        if (type.equals("items") || type.equals("map") || type.equals("character")) {
            getCommService().triggerUpload(webSocketContext.getPrincipal().getName(), type);
        } else {
            output.append("[red]Import of %s is not supported!", type);
            return question;
        }

        String principal = ch.getCreatedBy();

        Optional<User> optionalUser = userRepository.findUserByPrincipalName(principal);

        if (optionalUser.isEmpty()) {
            output.append("[red]Can't find that player.");
            return question;
        }

        User user = optionalUser.get();

        ReloadedUser reloadedUser = new ReloadedUser(
            user, String.format("%s %s been imported.", capitalize(type), type.equals("items") ? "have" : "has")
        );

        reloadedUsersRepository.save(reloadedUser);

        return question;
    }

    private static String capitalize(String input) {
        return input == null || input.isEmpty() ? input : input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}
