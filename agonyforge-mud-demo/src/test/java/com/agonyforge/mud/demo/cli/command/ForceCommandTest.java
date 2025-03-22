package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.repository.CommandForceRepository;
import com.agonyforge.mud.demo.model.repository.CommandRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@Disabled
public class ForceCommandTest {

    @Mock
    private RepositoryBundle repositoryBundle;

    @Mock
    private CommService commService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private WebSocketContext wsContext;

    @Mock
    private Question question;

    @Mock
    private MudCharacter ch;

    @Mock
    private CommandRepository commandRepository;

    @Mock
    private CommandForceRepository commandForceRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testNoArgs() {
        Output output = new Output();
        ForceCommand uut = new ForceCommand(repositoryBundle, commService, applicationContext, commandForceRepository, commandRepository);

        Question result = uut.execute(question, wsContext, List.of("FOR"), new Input("for"), output);

        assertEquals(question, result);

        verifyNoInteractions(commandRepository);
        verifyNoInteractions(commandForceRepository);
        verifyNoInteractions(commService);
    }
}
