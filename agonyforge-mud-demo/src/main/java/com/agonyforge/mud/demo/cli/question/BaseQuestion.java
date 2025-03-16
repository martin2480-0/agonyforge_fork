package com.agonyforge.mud.demo.cli.question;

import com.agonyforge.mud.core.cli.AbstractQuestion;
import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;

public abstract class BaseQuestion extends AbstractQuestion {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseQuestion.class);

    private final ApplicationContext applicationContext;
    private final RepositoryBundle repositoryBundle;

    public BaseQuestion(ApplicationContext applicationContext,
                        RepositoryBundle repositoryBundle) {
        super();
        this.applicationContext = applicationContext;
        this.repositoryBundle = repositoryBundle;
    }

    protected Question getQuestion(String name) {
        return applicationContext.getBean(name, Question.class);
    }

    protected RepositoryBundle getRepositoryBundle() {
        return repositoryBundle;
    }

    protected Optional<MudCharacter> getCharacter(WebSocketContext wsContext, Output output) {
        Long chId = (Long) wsContext.getAttributes().get(MUD_CHARACTER);
        Optional<MudCharacter> chOptional = getRepositoryBundle().getCharacterRepository().findById(chId);

        if (chOptional.isEmpty()) {
            LOGGER.error("Cannot look up character by ID: {}", chId);
            output.append("[red]Unable to find your character! The error has been reported.");
            return Optional.empty();
        }

        return chOptional;
    }

    protected Optional<Integer> getStatChangeFromInput(String input, boolean add) {
        char symbol = add ? '+' : '-';
        int lastIndex = input.lastIndexOf(symbol);

        int change = (lastIndex == input.length() - 1)
            ? (int) input.chars().filter(c -> c == symbol).count()
            : Integer.parseInt(input.substring(lastIndex + 1));

        return Optional.of(add ? change : -change);
    }

}
