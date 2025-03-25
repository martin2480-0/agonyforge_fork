package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.BannedUser;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.impl.User;
import com.agonyforge.mud.demo.model.repository.BannedUsersRepository;
import com.agonyforge.mud.demo.model.repository.UserRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class BanCommand extends AbstractCommand {

    private final UserRepository userRepository;
    BannedUsersRepository bannedUsersRepository;

    public BanCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext, BannedUsersRepository bannedUsersRepository, UserRepository userRepository) {
        super(repositoryBundle, commService, applicationContext);
        this.bannedUsersRepository = bannedUsersRepository;
        this.userRepository = userRepository;
    }

    public static Date getBanTime(Date date, String banTime) {
        int seconds = 0;

        Pattern pattern = Pattern.compile("(\\d+)([dhm])", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(banTime);

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            switch (unit) {
                case "d":
                    seconds += value * 86400;
                    break;
                case "h":
                    seconds += value * 3600;
                    break;
                case "m":
                    seconds += value * 60;
                    break;
            }
        }

        return new Date(date.getTime() + (seconds * 1000L));
    }

    // BAN add <user> PERM <důvod>
    // BAN add <user> TEMP <doba> <důvod>
    // BAN renew <ban_id> <doba>
    // BAN remove <ban_id>
    @Override
    public Question execute(Question question, WebSocketContext webSocketContext, List<String> tokens, Input input, Output output) {
        try {

            MudCharacter ch = getCurrentCharacter(webSocketContext, output);


            if (tokens.size() < 2) {
                output.append("[default]What ban action would you like to take?");
                return question;
            }

            String banAction = tokens.get(1);

            if (Stream.of("add", "renew", "remove", "list").noneMatch(banAction::equalsIgnoreCase)) {
                output.append("[red]Unknown ban action.");
                return question;
            }


            if (tokens.size() < 3 && !"list".equalsIgnoreCase(banAction)) {
                if (banAction.equalsIgnoreCase("add")) {
                    output.append("[default]Who would you like to ban?");
                    return question;
                } else if (banAction.equalsIgnoreCase("remove")) {
                    output.append("[default]Who would you like to unban?");
                    return question;
                } else if (banAction.equalsIgnoreCase("renew")) {
                    output.append("[default]Whose ban would you like renew?");
                    return question;
                }
            }

            if ("list".equalsIgnoreCase(banAction)) {

                List<BannedUser> bannedUsers = bannedUsersRepository.findAllByOrderByBannedOnAsc();

                output
                    .append("[white]Banned Users:")
                    .append("[default]Banned Users are listed from last banned to first.");

                bannedUsers.forEach(bannedUser -> {
                    output.append(String.format("[yellow]%-12s[white]: [red]%s\n",
                        bannedUser.getId(),
                        bannedUser.isPermanent() ? "Permanent Ban" : "Temporary Ban"));

                    if (!bannedUser.isPermanent()) {
                        output.append(String.format("[yellow]Banned Until[white]: [red]%s\n", bannedUser.getBannedToDate()));
                    }
                    String reason = bannedUser.getReason();

                    if (reason == null || reason.isEmpty()) {
                        reason = "No reason provided";
                    }
                    output.append(String.format("[yellow]Reason[white]: [red]%s\n", reason));
                    output.append(String.format("[yellow]Banned On[white]: [red]%s\n", bannedUser.getBannedOn()));
                });


                return question;
            }

            if (tokens.size() < 3 && "add".equalsIgnoreCase(banAction)) {
                output.append("[red]You must specify a ban type.");
                return question;
            }

            if (tokens.size() < 4 && "renew".equalsIgnoreCase(banAction)) {
                output.append("[red]You must specify a ban renewal duration.");
            }


            String userIdentification = tokens.get(2);

            if (banAction.equalsIgnoreCase("add")) {
                Optional<MudCharacter> targetOptional = findWorldCharacter(ch, userIdentification);

                if (targetOptional.isEmpty() || targetOptional.get().getPlayer() == null) {
                    output.append("[red]Can't find that player.");
                    return question;
                }

                MudCharacter target = targetOptional.get();

                boolean perm = false;
                String type = tokens.get(3);
                if ("perm".equalsIgnoreCase(type)) {
                    perm = true;
                }
                if (tokens.size() < 5 && !perm) {
                    output.append("[red]You must specify a ban duration.");
                    return question;
                }

                Date date;
                String reason;
                if (perm) {
                    date = new Date();
                    reason = String.join(" ", tokens.subList(4, tokens.size()));
                } else {
                    String duration = tokens.get(4);
                    date = getBanTime(new Date(), duration);
                    reason = String.join(" ", tokens.subList(5, tokens.size()));
                }

                String principal = target.getCreatedBy();

                Optional<User> optionalUser = userRepository.findUserByPrincipalName(principal);

                if (optionalUser.isEmpty()) {
                    output.append("[red]User not found.");
                    return question;
                }

                User user = optionalUser.get();

                BannedUser bannedUserInstance = new BannedUser(user, perm, date, reason);

                bannedUsersRepository.save(bannedUserInstance);

                getCommService().sendToRoom(ch.getLocation().getRoom().getId(),
                    new Output("[yellow]%s disappears in a puff of smoke!", target.getCharacter().getName()), ch);

                webSocketContext.getAttributes().put("force_user", target.getCharacter().getId());

                Command command;
                try {
                    command = this.getApplicationContext().getBean("quitCommand", Command.class);
                } catch (NoSuchBeanDefinitionException e) {
                    output.append("[red]Command not found");
                    return question;
                }

                String fullCommand = "quit now";

                List<String> tokensKick = new ArrayList<>();

                tokensKick.add("QUIT");
                tokensKick.add("NOW");

                command.execute(question, webSocketContext, tokensKick, new Input(fullCommand), new Output());

                webSocketContext.getAttributes().remove("force_user");

                LOGGER.info("{} has been banned.", ch.getCharacter().getName());

                getCommService().sendToAll(webSocketContext,
                    new Output("[yellow]%s has been banned!", ch.getCharacter().getName()), ch);

                return question;
            } else if (banAction.equalsIgnoreCase("renew")) {
                Long id = Long.parseLong(userIdentification);

                Optional<BannedUser> bannedUserOptional = bannedUsersRepository.findById(id);

                if (bannedUserOptional.isEmpty()) {
                    output.append("[red]Can't find player with that ban ID.");
                    return question;
                }

                BannedUser bannedUser = bannedUserOptional.get();

                if (bannedUser.isPermanent()) {
                    output.append("[red]Can't renew permanent ban.");
                    return question;
                }

                Date currentBanEnd = bannedUser.getBannedToDate();
                String duration = tokens.get(3);
                Date newBanEnd = getBanTime(currentBanEnd, duration);

                bannedUser.setBannedToDate(newBanEnd);

                bannedUsersRepository.save(bannedUser);
            } else if (banAction.equalsIgnoreCase("remove")) {
                Long id = Long.parseLong(userIdentification);

                Optional<BannedUser> bannedUserOptional = bannedUsersRepository.findById(id);

                if (bannedUserOptional.isEmpty()) {
                    output.append("[red]Can't find player with that ban ID.");
                    return question;
                }

                BannedUser bannedUser = bannedUserOptional.get();

                bannedUsersRepository.delete(bannedUser);
            }


            return question;
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return question;
    }
}
