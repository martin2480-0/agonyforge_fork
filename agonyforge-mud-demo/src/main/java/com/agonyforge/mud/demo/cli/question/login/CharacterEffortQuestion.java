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
import com.agonyforge.mud.demo.model.constant.Effort;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CharacterEffortQuestion extends BaseQuestion {
    public static final int STARTING_EFFORTS = 4;

    private final MenuPane menuPane = new MenuPane();

    public CharacterEffortQuestion(ApplicationContext applicationContext,
                                   RepositoryBundle repositoryBundle) {
        super(applicationContext, repositoryBundle);

        menuPane.setTitle(new MenuTitle("Allocate Effort Bonuses"));
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
        String nextQuestion = "characterEffortQuestion";
        Output output = new Output();
        MudCharacter ch = getCharacter(webSocketContext, output).orElseThrow();
        String choice = input.getInput().toUpperCase(Locale.ROOT);
        int totalPoints = computeEffortPoints(ch);

        if (choice.contains("+")) {
            if (totalPoints >= STARTING_EFFORTS) {
                output.append("[red]You don't have any more points to allocate!");
            } else {
                try {
                    int effortIndex = Integer.parseInt(choice.substring(0, 1)) - 1;
                    Optional<Integer> add = getStatChangeFromInput(choice, true);
                    if (add.isEmpty()) {
                        throw new NumberFormatException();
                    }
                    int addCount = add.get();
                    if (totalPoints + addCount > STARTING_EFFORTS) {
                        int lessCount = totalPoints + addCount - STARTING_EFFORTS;
                        output.append("[red]You can't allocate this many points. Allocate %d less!", lessCount);
                    } else {
                        ch.getCharacter().addBaseEffort(Effort.values()[effortIndex], addCount);
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    output.append("[red]Oops! Try a number with a plus or minus!");
                }
            }
        } else if (choice.contains("-")) {
            HashMap<Effort, Integer> currentEffortPoints = getEffortValues(ch);
            try {
                int effortIndex = Integer.parseInt(choice.substring(0, 1)) - 1;
                Optional<Integer> remove = getStatChangeFromInput(choice, false);
                if (remove.isEmpty()) {
                    throw new NumberFormatException();
                }
                int removeCount = remove.get();
                Effort editedEffort = Effort.values()[effortIndex];
                if (currentEffortPoints.get(editedEffort) + removeCount < 0) {
                    output.append("[red]You can't make your stats negative!");
                } else {
                    ch.getCharacter().addBaseEffort(editedEffort, removeCount);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                output.append("[red]Oops! Try a number with a plus or minus!");
            }

        } else {
            if (choice.equals("S")) {
                if (totalPoints == STARTING_EFFORTS) {
                    output.append("[green]Character efforts saved!");
                    nextQuestion = "characterSpeciesQuestion";
                } else {
                    output.append("[red]Please allocate exactly %d points for your efforts.", STARTING_EFFORTS);
                }
            } else {
                output.append("[red]Oops! Try a number with a plus or minus!");
            }
        }

        getRepositoryBundle().getCharacterRepository().save(ch);

        Question next = getQuestion(nextQuestion);

        return new Response(next, output);
    }

    private int computeEffortPoints(MudCharacter ch) {
        return Arrays.stream(Effort.values())
            .map(ch.getCharacter()::getBaseEffort)
            .reduce(0, Integer::sum);
    }

    private HashMap<Effort, Integer> getEffortValues(MudCharacter ch) {
        return Arrays.stream(Effort.values())
            .collect(Collectors.toMap(effort -> effort, effort -> ch.getCharacter()
                .getBaseEffort(effort), (a, b) -> b, HashMap::new));
    }

    private void populateMenuItems(MudCharacter ch) {
        menuPane.getItems().clear();

        int points = STARTING_EFFORTS - computeEffortPoints(ch);

        menuPane.getItems().add(new MenuItem(" ", "[default]Enter the menu number and a plus (+) or minus (-) to add or subtract from an effort. For example, '1+' to raise 'Basic' or '5-' to lower 'Ultimate'."));
        menuPane.getItems().add(new MenuItem(" ", String.format("[default]Please allocate [white]%d more points [default]for your stats.", points)));

        Arrays.stream(Effort.values())
            .forEachOrdered(effort -> menuPane.getItems().add(new MenuItem((effort.ordinal() + 1) + "[+/-]", String.format("%16s (%d)", effort.getName(), ch.getCharacter().getBaseEffort(effort)))));

        menuPane.getItems().add(new MenuItem("S", "Save"));
    }
}
