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
public class SlayCommand extends AbstractCommand {

    private final UserRepository userRepository;
    private final ReloadedUsersRepository reloadedUsersRepository;

    @Autowired
    public SlayCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext, UserRepository userRepository, ReloadedUsersRepository reloadedUsersRepository) {
        super(repositoryBundle, commService, applicationContext);
        this.userRepository = userRepository;
        this.reloadedUsersRepository = reloadedUsersRepository;
    }

    @Override
    public Question execute(Question question, WebSocketContext webSocketContext, List<String> tokens, Input input, Output output) {
        MudCharacter ch = getCurrentCharacter(webSocketContext, output);

        if (tokens.size() < 2) {
            output.append("[default]Whom do you wish to slay?");
            return question;
        }

        Optional<MudCharacter> targetOptional = findRoomCharacter(ch, tokens.get(1));

        if (targetOptional.isEmpty()) {
            output.append("[default]You don't see anyone like that.");
            return question;
        }

        MudCharacter target = targetOptional.get();
        getRepositoryBundle().getCharacterRepository().delete(target);

        String principal = target.getCreatedBy();

        Optional<User> optionalUser = userRepository.findUserByPrincipalName(principal);

        if (optionalUser.isEmpty()) {
            output.append("[red]Can't find that player.");
            return question;
        }

        User targetUser = optionalUser.get();

        ReloadedUser reloadedUser = new ReloadedUser(targetUser, String.format("Your character %s has been slayed!", target.getCharacter().getName()));

        reloadedUsersRepository.save(reloadedUser);

        getCommService().reloadUser(principal);

        output.append("[yellow]You snap your fingers, and %s disappears!", target.getCharacter().getName());
        getCommService().sendToRoom(ch.getLocation().getRoom().getId(),
            new Output("[yellow]%s snaps %s fingers, and %s disappears!",
                ch.getCharacter().getName(),
                ch.getCharacter().getPronoun().getPossessive(),
                target.getCharacter().getName()), ch);



        return question;
    }
}
