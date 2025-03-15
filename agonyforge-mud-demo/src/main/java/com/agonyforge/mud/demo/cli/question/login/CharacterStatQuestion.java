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
import com.agonyforge.mud.demo.model.constant.Stat;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class CharacterStatQuestion extends BaseQuestion {
    public static final int STARTING_STATS = 6;

    private final MenuPane menuPane = new MenuPane();

    private final static String errorString = "[red][red]Oops! Try these versions: 1+, 1++, 1-, 1+2";

    public CharacterStatQuestion(ApplicationContext applicationContext,
                                 RepositoryBundle repositoryBundle) {
        super(applicationContext, repositoryBundle);

        menuPane.setTitle(new MenuTitle("Allocate Stat Points"));
        menuPane.setPrompt(new MenuPrompt());
    }

    @Override
    public Output prompt(WebSocketContext wsContext) {
        Output output = new Output();
        MudCharacter ch = getCharacter(wsContext, output).orElseThrow();

        populateMenuItems(ch);

        return menuPane.render(Color.WHITE, Color.BLACK);
    }

    @Override
    public Response answer(WebSocketContext webSocketContext, Input input) {
        String nextQuestion = "characterStatQuestion";
        Output output = new Output();
        MudCharacter ch = getCharacter(webSocketContext, output).orElseThrow();
        String choice = input.getInput().toUpperCase(Locale.ROOT);
        int totalPoints = computeStatPoints(ch);


        if (choice.contains("+")) {
            if (totalPoints >= STARTING_STATS) {
                output.append("[red]You don't have any more points to allocate!");
            } else {
                try {
                    int statIndex = Integer.parseInt(choice.substring(0, 1)) - 1;

                    int add;
                    int lastPlusIndex = choice.lastIndexOf("+");
                    if (lastPlusIndex == choice.length() - 1) {
                        int plusCount = 0;
                        for (char c : choice.toCharArray()) {
                            if (c == '+') {
                                plusCount++;
                            }
                        }
                        add = plusCount;
                    } else {
                        add = Integer.parseInt(choice.substring(lastPlusIndex + 1));
                    }
                    if (totalPoints + add > STARTING_STATS) {
                        int lessCount = totalPoints + add - STARTING_STATS;
                        output.append("[red]You can't allocate this many points. Allocate %d less!", lessCount);
                    }else {
                        ch.getCharacter().addBaseStat(Stat.values()[statIndex], add);
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    output.append(errorString);
                }
            }
        } else if (choice.contains("-")) {
            HashMap<Stat, Integer> currentStatPoints = getStatValues(ch);
            try {
                int statIndex = Integer.parseInt(choice.substring(0, 1)) - 1;
                int remove;
                int lastPlusIndex = choice.lastIndexOf("-");
                if (lastPlusIndex == choice.length() - 1) {
                    int minusCount = 0;
                    for (char c : choice.toCharArray()) {
                        if (c == '-') {
                            minusCount++;
                        }
                    }
                    remove = minusCount;
                } else {
                    remove = Integer.parseInt(choice.substring(lastPlusIndex + 1));
                }
                Stat editedStat = Stat.values()[statIndex];
                System.out.println(remove);
                if (currentStatPoints.get(editedStat) - remove < 0) {
                    output.append("[red]You can't make your stats negative!");
                } else {
                    ch.getCharacter().addBaseStat(editedStat, -remove);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                output.append(errorString);
            }
        } else {
            if (choice.equals("S")) {
                if (totalPoints == STARTING_STATS) {
                    output.append("[green]Character stats saved!");
                    nextQuestion = "characterEffortQuestion";
                } else {
                    output.append("[red]Please allocate exactly %d points for your stats.", STARTING_STATS);
                }
            } else {
                output.append(errorString);
            }
        }

        getRepositoryBundle().getCharacterRepository().save(ch);

        Question next = getQuestion(nextQuestion);

        return new Response(next, output);
    }

    private int computeStatPoints(MudCharacter ch) {
        return Arrays.stream(Stat.values())
            .map(ch.getCharacter()::getBaseStat)
            .reduce(0, Integer::sum);
    }

    private HashMap<Stat, Integer> getStatValues(MudCharacter ch) {
        return Arrays.stream(Stat.values())
            .collect(Collectors.toMap(stat -> stat, stat -> ch.getCharacter()
                .getBaseStat(stat), (a, b) -> b, HashMap::new));
    }

    private void populateMenuItems(MudCharacter ch) {
        menuPane.getItems().clear();

        int points = STARTING_STATS - computeStatPoints(ch);

        menuPane.getItems().add(new MenuItem(" ", "[default]Enter the menu number and a plus (+) or minus (-) to add or subtract from a stat. For example, '3+' to raise CON or '6-' to lower CHA."));
        menuPane.getItems().add(new MenuItem(" ", String.format("[default]Please allocate [white]%d more points [default]for your stats.", points)));

        Arrays.stream(Stat.values())
            .forEachOrdered(stat -> menuPane.getItems().add(new MenuItem((stat.ordinal() + 1) + "[+/-]", String.format("%15s (%d)", stat.getName(), ch.getCharacter().getBaseStat(stat)))));

        menuPane.getItems().add(new MenuItem("S", "Save"));
    }
}
