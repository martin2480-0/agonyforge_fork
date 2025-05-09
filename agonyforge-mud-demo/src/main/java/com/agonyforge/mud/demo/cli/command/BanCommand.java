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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class BanCommand extends AbstractCommand {

    private final UserRepository userRepository;
    private final BannedUsersRepository bannedUsersRepository;

    public BanCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext, BannedUsersRepository bannedUsersRepository, UserRepository userRepository) {
        super(repositoryBundle, commService, applicationContext);
        this.bannedUsersRepository = bannedUsersRepository;
        this.userRepository = userRepository;
    }

    public static Date getBanTime(Date date, String banTime) {
        if (date == null || banTime == null || banTime.isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile("^(\\d+d)?(\\d+h)?(\\d+m)?$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(banTime);

        if (!matcher.matches()) {
            return null;
        }

        int seconds = 0;

        if (matcher.group(1) != null) {
            seconds += Integer.parseInt(matcher.group(1).replaceAll("(?i)d", "")) * 86400;
        }
        if (matcher.group(2) != null) {
            seconds += Integer.parseInt(matcher.group(2).replaceAll("(?i)h", "")) * 3600;
        }
        if (matcher.group(3) != null) {
            seconds += Integer.parseInt(matcher.group(3).replaceAll("(?i)m", "")) * 60;
        }


        if (seconds <= 0) {
            return null;
        }

        return new Date(date.getTime() + (seconds * 1000L));
    }


    private static String getTimeRemaining(Date futureDate) {
        Date now = new Date();
        long differenceInMillis = futureDate.getTime() - now.getTime();

        long days = TimeUnit.MILLISECONDS.toDays(differenceInMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(differenceInMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis) % 60;

        StringBuilder timeRemaining = new StringBuilder();

        if (days > 0) {
            timeRemaining.append(days).append(days == 1 ? " day " : " days ");
        }
        if (hours > 0) {
            timeRemaining.append(hours).append(hours == 1 ? " hour " : " hours ");
        }
        if (minutes > 0) {
            timeRemaining.append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }

        return "Time remaining: " + timeRemaining.toString().trim();
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

                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

                List<BannedUser> bannedUsers = bannedUsersRepository.findAllByOrderByBannedOnAsc();

                output
                    .append("[white]Banned Users:")
                    .append("[default]Banned Users are listed from last banned to first.");

                bannedUsers.forEach(bannedUser -> {
                    output.append(String.format("[yellow]%-12s[white]: [red]%s\n",
                        bannedUser.getId(),
                        bannedUser.isPermanent() ? "Permanent Ban" : "Temporary Ban"));

                    String formattedBannedOnDate = formatter.format(bannedUser.getBannedOn());
                    output.append(String.format("Banned On: [red]%s\n", formattedBannedOnDate));
                    if (!bannedUser.isPermanent()) {
                        String formattedBannedToDate = formatter.format(bannedUser.getBannedToDate());
                        output.append(String.format("Banned Until: [red]%s\n", formattedBannedToDate));
                        output.append("Time remaining: %s", getTimeRemaining(bannedUser.getBannedToDate()));
                    }

                    String reason = bannedUser.getReason();

                    if (reason == null || reason.isEmpty()) {
                        reason = "No reason provided";
                    }
                    output.append(String.format("Reason: [red]%s\n", reason));
                    output.append("**************************************************");
                });


                return question;
            }

            if (tokens.size() < 4 && "add".equalsIgnoreCase(banAction)) {
                output.append("[red]You must specify a ban type.");
                return question;
            }

            if (tokens.size() < 4 && "renew".equalsIgnoreCase(banAction)) {
                output.append("[red]You must specify a ban renewal duration.");
                return question;
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

                    if (date == null) {
                        output.append("[red]Invalid time format.");
                        return question;
                    }

                    String[] words = input.getInput().split("\\s+");
                    reason = words.length < 6 ? "" : String.join(" ", Arrays.copyOfRange(words, 5, words.length));
                }

                String principal = target.getCreatedBy();

                Optional<User> optionalUser = userRepository.findUserByPrincipalName(principal);

                if (optionalUser.isEmpty()) {
                    output.append("[red]User not found.");
                    return question;
                }

                User user = optionalUser.get();

                target.setLocation(null);

                getRepositoryBundle().getCharacterRepository().save(target);

                BannedUser bannedUserInstance = new BannedUser(user, perm, date, reason);

                bannedUsersRepository.save(bannedUserInstance);

                getCommService().sendToRoom(ch.getLocation().getRoom().getId(),
                    new Output("[yellow]%s disappears in a puff of smoke!", target.getCharacter().getName()), ch);

                getCommService().reloadUser(principal);

                LOGGER.info("{} has been banned!", target.getCharacter().getName());

                output.append("[yellow]%s has been banned!", target.getCharacter().getName());

                getCommService().sendToAll(webSocketContext,
                    new Output("[yellow]%s has been banned!", target.getCharacter().getName()), ch);

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

                if (newBanEnd == null) {
                    output.append("[red]Invalid time format.");
                    return question;
                }

                output.append("[yellow]Ban has been lengthened by %s.", duration.toLowerCase());


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

                output.append("[yellow]Player with id %s has been unbanned.", bannedUser.getId());

                bannedUsersRepository.delete(bannedUser);
            }


            return question;
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return question;
    }
}
