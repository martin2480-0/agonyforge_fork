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
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class CharacterImportQuestion extends BaseQuestion {
    private final MenuPane menuPane = new MenuPane();
    private final CommService commService;

    @Autowired
    public CharacterImportQuestion(ApplicationContext applicationContext,
                                   RepositoryBundle repositoryBundle, CommService commService) {
        super(applicationContext, repositoryBundle);
        this.commService = commService;

        menuPane.setPrompt(new MenuPrompt());

    }

    private void populateMenuItems() {
        menuPane.getItems().clear();

        menuPane.getItems().add(new MenuItem("1", "Import"));
        menuPane.getItems().add(new MenuItem("B", "Cancel"));
    }


    @Override
    public Output prompt(WebSocketContext wsContext) {
        menuPane.setTitle(new MenuTitle("Choose one of the following options:"));

        populateMenuItems();

        return menuPane.render(Color.WHITE, Color.BLACK);
    }

    @Override
    public Response answer(WebSocketContext wsContext, Input input) {
        populateMenuItems();

        String nextQuestion = "characterImportQuestion";
        Output output = new Output();
        String choice = input.getInput().toUpperCase();
        Optional<MenuItem> itemOptional = menuPane.getItems()
            .stream()
            .map(i -> (MenuItem) i)
            .filter(i -> choice.equals(i.getKey()))
            .findFirst();

        if (itemOptional.isEmpty()) {
            output.append("[red]Please choose one of the menu options.");
            nextQuestion = "characterImportQuestion";
        } else if ("1".equals(choice)) {
            commService.triggerUpload(wsContext.getPrincipal().getName(), "character");
            nextQuestion = "characterImportQuestion";
        } else if ("B".equals(choice)) {
            nextQuestion = "characterMenuQuestion";
        }
        else {
            output.append("[red]Please choose one of the menu options.");
        }

        Question next = getQuestion(nextQuestion);

        return new Response(next, output);
    }
}
