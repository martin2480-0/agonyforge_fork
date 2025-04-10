package com.agonyforge.mud.demo.cli.question.login;

import com.agonyforge.mud.core.cli.Color;
import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.cli.Response;
import com.agonyforge.mud.core.cli.menu.impl.MenuItem;
import com.agonyforge.mud.core.cli.menu.impl.MenuPane;
import com.agonyforge.mud.core.cli.menu.impl.MenuPrompt;
import com.agonyforge.mud.core.cli.menu.impl.MenuTitle;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.cli.question.BaseQuestion;
import com.agonyforge.mud.demo.model.impl.BannedUser;
import com.agonyforge.mud.demo.model.impl.ReloadedUser;
import com.agonyforge.mud.demo.model.repository.BannedUsersRepository;
import com.agonyforge.mud.demo.model.repository.ReloadedUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;

@Component
public class CharacterMenuQuestion extends BaseQuestion {
    private final MenuPane menuPane = new MenuPane();
    private final BannedUsersRepository bannedUsersRepository;
    private final ReloadedUsersRepository reloadedUsersRepository;

    @Autowired
    public CharacterMenuQuestion(ApplicationContext applicationContext,
                                 RepositoryBundle repositoryBundle, BannedUsersRepository bannedUsersRepository, ReloadedUsersRepository reloadedUsersRepository) {
        super(applicationContext, repositoryBundle);
        this.bannedUsersRepository = bannedUsersRepository;
        this.reloadedUsersRepository = reloadedUsersRepository;

        menuPane.setPrompt(new MenuPrompt());
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

    @Override
    public Output prompt(WebSocketContext wsContext) {
        Principal principal = wsContext.getPrincipal();

        String reloadReason = null;
        Optional<ReloadedUser> reloadedUserOptional = reloadedUsersRepository.findByReloadedUser_PrincipalName(principal.getName());
        if (reloadedUserOptional.isPresent()) {
            ReloadedUser reloadedUser = reloadedUserOptional.get();
            reloadReason = reloadedUser.getReason();
            reloadedUsersRepository.delete(reloadedUser);
        }

        Optional<BannedUser> bannedUserOptional = bannedUsersRepository.findByBannedUser_PrincipalName(principal.getName());

        if (bannedUserOptional.isPresent()) {

            BannedUser bannedUser = bannedUserOptional.get();

            String banReason = bannedUser.getReason();

            String banType = bannedUser.isPermanent() ? "permanent" : "temporary";

            if (banReason.isBlank()) {
                banReason = "Not given";
            }

            menuPane.setTitle(new MenuTitle("You have been banned!"));

            menuPane.getItems().clear();

            menuPane.getItems().add(new MenuItem("", String.format("Reason: %s", banReason)));
            menuPane.getItems().add(new MenuItem("", String.format("Type: %s", banType)));
            if (!bannedUser.isPermanent()) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

                String formattedStartDate = formatter.format(bannedUser.getBannedOn());
                String formattedBannedToDate = formatter.format(bannedUser.getBannedToDate());

                String timeRemaining = getTimeRemaining(bannedUser.getBannedToDate());

                menuPane.getItems().add(new MenuItem("", String.format("Start date: %s", formattedStartDate)));
                menuPane.getItems().add(new MenuItem("", String.format("End date: %s", formattedBannedToDate)));
                menuPane.getItems().add(new MenuItem("", timeRemaining));
            }
            return menuPane.render(Color.WHITE, Color.BLACK);
        }

        populateMenuItems(principal, reloadReason);

        menuPane.setTitle(new MenuTitle("Your Characters"));
        return menuPane.render(Color.WHITE, Color.BLACK);
    }

    @Override
    public Response answer(WebSocketContext wsContext, Input input) {

        if (bannedUsersRepository.findByBannedUser_PrincipalName(wsContext.getPrincipal().getName()).isPresent()) {
            Question next = getQuestion("characterMenuQuestion");
            return new Response(next, new Output());
        }

        populateMenuItems(wsContext.getPrincipal());

        String nextQuestion = "characterMenuQuestion";
        Output output = new Output();
        String choice = input.getInput().toUpperCase();
        Optional<MenuItem> itemOptional = menuPane.getItems()
            .stream()
            .map(i -> (MenuItem) i)
            .filter(i -> choice.equals(i.getKey()))
            .findFirst();

        if (itemOptional.isEmpty()) {
            output.append("[red]Please choose one of the menu options.");
        } else if ("N".equals(choice)) {
            nextQuestion = "characterNameQuestion";
        } else if ("I".equals(choice)) {
            nextQuestion = "characterImportQuestion";
        }
        else {
            MenuItem item = itemOptional.get();
            wsContext.getAttributes().put(MUD_CHARACTER, item.getItem());
            nextQuestion = "characterViewQuestion";
        }

        Question next = getQuestion(nextQuestion);

        return new Response(next, output);
    }

    private void populateMenuItems(Principal principal) {
        populateMenuItems(principal, null);
    }

    private void populateMenuItems(Principal principal, String reason) {
        menuPane.getItems().clear();

        if (reason != null && !reason.isEmpty()) {
            menuPane.getItems().add(new MenuItem("", String.format("[red]%s", reason)));
        }

        menuPane.getItems().add(new MenuItem("N", "New Character"));
        menuPane.getItems().add(new MenuItem("I", "Import Character"));

        getRepositoryBundle().getCharacterRepository().findByPlayerUsername(principal.getName())
            .forEach(ch -> {
                boolean playing = ch.getLocation() != null;

                menuPane.getItems().add(new MenuItem(
                    Integer.toString(menuPane.getItems().size() - 1),
                    String.format("%s%s%s",
                        ch.getId() == 1L ? "[yellow]" : "[white]",
                        ch.getCharacter().getName(),
                        playing ? " [dgreen]*[green]PLAYING[dgreen]*" : ""),
                    ch.getId()));
            });
    }
}
