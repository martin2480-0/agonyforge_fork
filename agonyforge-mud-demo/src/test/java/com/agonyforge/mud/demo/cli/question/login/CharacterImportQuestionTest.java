package com.agonyforge.mud.demo.cli.question.login;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.cli.Response;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.model.repository.MudItemRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CharacterImportQuestionTest {
    @Mock
    private RepositoryBundle repositoryBundle;

    @Mock
    private Principal principal;

    @Mock
    private Question question;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private MudCharacterRepository characterRepository;

    @Mock
    private MudItemRepository itemRepository;

    @Mock
    private WebSocketContext webSocketContext;

    @Mock
    private CommService commService;

    @BeforeEach
    void setUp() {
        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(characterRepository);
        lenient().when(repositoryBundle.getItemRepository()).thenReturn(itemRepository);
    }

    @Test
    void testPrompt() {

        CharacterImportQuestion uut = new CharacterImportQuestion(
            applicationContext,
            repositoryBundle,
            commService);

        Output result = uut.prompt(webSocketContext);
        Optional<String> importCharacterOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains("Import"))
            .findFirst();

        Optional<String> cancelOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains("Cancel"))
            .findFirst();


        assertEquals(8, result.getOutput().size());
        assertTrue(importCharacterOptional.isPresent());
        assertTrue(cancelOptional.isPresent());

    }

    @Test
    void testPromptTwice() {

        CharacterImportQuestion uut = new CharacterImportQuestion(
            applicationContext,
            repositoryBundle,
            commService);

        uut.prompt(webSocketContext);
        uut.prompt(webSocketContext);
        Output result = uut.prompt(webSocketContext);

        List<String> lines = result.getOutput()
            .stream()
            .filter(line -> line.contains("Import"))
            .toList();

        assertEquals(1, lines.size());
    }

    @Test
    void testAnswerEmpty() {
        CharacterImportQuestion uut = new CharacterImportQuestion(
            applicationContext,
            repositoryBundle,
            commService);

        when(applicationContext.getBean(eq("characterImportQuestion"), eq(Question.class))).thenReturn(uut);

        Response result = uut.answer(webSocketContext, new Input(""));

        assertEquals(uut, result.getNext());
        verify(webSocketContext, never()).getAttributes();
    }

    @Test
    void testAnswerImport() {
        when(applicationContext.getBean(eq("characterImportQuestion"), eq(Question.class))).thenReturn(question);

        CharacterImportQuestion uut = new CharacterImportQuestion(
            applicationContext, repositoryBundle, commService);
        Response result = uut.answer(webSocketContext, new Input("i"));

        assertEquals(question, result.getNext());

        verify(webSocketContext, never()).getAttributes();
    }

    @Test
    void testAnswerCancel() {
        when(applicationContext.getBean(eq("characterMenuQuestion"), eq(Question.class))).thenReturn(question);

        CharacterImportQuestion uut = new CharacterImportQuestion(
            applicationContext, repositoryBundle, commService);

        Response result = uut.answer(webSocketContext, new Input("b"));

        assertEquals(question, result.getNext());

        verify(webSocketContext, never()).getAttributes();
    }

    @Test
    void testAnswerNonValid() {
        CharacterImportQuestion uut = new CharacterImportQuestion(
            applicationContext, repositoryBundle, commService);

        when(applicationContext.getBean(eq("characterImportQuestion"), eq(Question.class))).thenReturn(uut);

        Response result = uut.answer(webSocketContext, new Input("9"));

        assertEquals(uut, result.getNext());

        verify(webSocketContext, never()).getAttributes();
    }

}
