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
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class BanCommand extends AbstractCommand {

    BannedUsersRepository bannedUsersRepository;

    public BanCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext, BannedUsersRepository bannedUsersRepository) {
        super(repositoryBundle, commService, applicationContext);
        this.bannedUsersRepository = bannedUsersRepository;
    }

    // BAN add <user> PERM <důvod>
    // BAN add <user> TEMP <doba> <důvod>
    // BAN renew <ban_id> <doba>
    // BAN remove <ban_id>
    @Override
    public Question execute(Question question, WebSocketContext webSocketContext, List<String> tokens, Input input, Output output) {
        MudCharacter ch = getCurrentCharacter(webSocketContext, output);


        if (tokens.size() < 2) {
            output.append("[default]What ban action would you like to take?");
            return question;
        }

        String banAction = tokens.get(1);

        if (tokens.size() < 3 && !"list".equalsIgnoreCase(banAction)) {
            output.append("[red]Unknown ban action.");
            return question;
        }

        if ("list".equalsIgnoreCase(banAction)) {

            List<BannedUser> bannedUsers = bannedUsersRepository.findAllByOrderByBannedOnAsc();

            output
                .append("[white]Banned Users:")
                .append("[default]Banned Users are listed from last banned to first.");

        bannedUsers.forEach(bannedUser -> {
                output.append(String.format("[yellow]%-12s[white]: [red]%s\n",
                    bannedUser.getBannedUser().getGivenName(),
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

        if (tokens.size() < 4 && "add".equalsIgnoreCase(banAction)) {
            output.append("[red]You must specify a ban type.");
            return question;
        }

        if (tokens.size() < 4 && "renew".equalsIgnoreCase(banAction)) {
            output.append("[red]You must specify a ban renewal duration.");
        }

        Optional<MudCharacter> targetOptional = findWorldCharacter(ch, tokens.get(1));

        if (targetOptional.isEmpty() || targetOptional.get().getPlayer() == null) {
            output.append("[red]Can't find that player.");
            return question;
        }

        MudCharacter target = targetOptional.get();

        String user = tokens.get(2);

        switch (banAction) {
            case "add" -> {
                boolean perm = false;
                if (tokens.size() < 5) {
                    output.append("[red]You must specify a ban duration.");
                    return question;
                }

                String type = tokens.get(3);
                if ("perm".equalsIgnoreCase(type)) {
                    perm = true;
                }
                Date date;
                String reason;
                if (perm){
                    date = new Date(Long.MAX_VALUE);
                    reason = String.join(" ", tokens.subList(4, tokens.size()));
                }else {
                    String duration = tokens.get(4);
                    date = getBanTime(new Date(), duration);
                    reason = String.join(" ", tokens.subList(5, tokens.size()));
                }

                BannedUser bannedUserInstance = new BannedUser(new User(), perm, date, reason);

                bannedUsersRepository.save(bannedUserInstance);

                getCommService().sendToRoom(ch.getLocation().getRoom().getId(),
                    new Output("[yellow]%s disappears in a puff of smoke!", target.getCharacter().getName()), ch);

            }
            case "renew" -> {
                Long id = Long.parseLong(user);

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


            }
            case "remove" -> {
                Long id = Long.parseLong(user);

                Optional<BannedUser> bannedUserOptional = bannedUsersRepository.findById(id);

                if (bannedUserOptional.isEmpty()) {
                    output.append("[red]Can't find player with that ban ID.");
                    return question;
                }

                BannedUser bannedUser = bannedUserOptional.get();

                bannedUsersRepository.delete(bannedUser);

            }
        }

        return question;
    }

    public static Date getBanTime(Date date, String banTime) {
        int seconds = 0;

        if (banTime.contains("d")) {
            String[] parts = banTime.split("d");
            int days = Integer.parseInt(parts[0]);
            seconds += (days * 86400);
        }

        if (banTime.contains("h")) {
            String[] parts = banTime.split("h");
            int hours = Integer.parseInt(parts[0]);
            seconds += (hours * 3600);
        }

        if (banTime.contains("m")) {
            String[] parts = banTime.split("m");
            int minutes = Integer.parseInt(parts[0]);
            seconds += (minutes * 60);
        }

        long futureTimeMillis = date.getTime() + (seconds * 1000L);

        return new Date(futureTimeMillis);
    }
}
