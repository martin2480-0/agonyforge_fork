package com.agonyforge.mud.demo.cli.question.login;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.cli.Response;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.Effort;
import com.agonyforge.mud.demo.model.constant.Stat;
import com.agonyforge.mud.demo.model.impl.CharacterComponent;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static com.agonyforge.mud.demo.cli.question.login.CharacterEffortQuestion.STARTING_EFFORTS;
import static com.agonyforge.mud.demo.cli.question.login.CharacterStatQuestion.STARTING_STATS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CharacterEffortQuestionTest {
    private final Random random = new Random();

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private RepositoryBundle repositoryBundle;

    @Mock
    private WebSocketContext wsContext;

    @Mock
    private MudCharacterRepository mudCharacterRepository;

    @Mock
    private MudCharacter ch;

    @Mock
    private CharacterComponent characterComponent;

    @Mock
    private Question question;

    @Mock
    private Question nextQuestion;

    @BeforeEach
    void setUp() {
        lenient().when(applicationContext.getBean(eq("characterEffortQuestion"), eq(Question.class))).thenReturn(question);
        lenient().when(applicationContext.getBean(eq("characterSpeciesQuestion"), eq(Question.class))).thenReturn(nextQuestion);

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);
    }

    @Test
    void testPrompt() {
        Long chId = random.nextLong();

        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Output result = uut.prompt(wsContext);

        assertTrue(result.getOutput().size() >= 14);
    }

    @Test
    void testAdd() {
        Long chId = random.nextLong();

        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("1+"));

        verify(characterComponent).addBaseEffort(eq(Effort.BASIC), eq(1));
        verify(mudCharacterRepository).save(eq(ch));

        assertTrue(response.getFeedback().isPresent());
        assertEquals(question, response.getNext());
    }

    @Test
    void testAddWithMultiplePluses() {
        Long chId = random.nextLong();

        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));;

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("1++"));

        verify(characterComponent).addBaseEffort(eq(Effort.BASIC), eq(2));
        verify(mudCharacterRepository).save(eq(ch));

        assertTrue(response.getFeedback().isPresent());
        assertEquals(question, response.getNext());
    }

    @Test
    void testAddWithNumber() {
        Long chId = random.nextLong();

        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("1+2"));

        verify(characterComponent).addBaseEffort(eq(Effort.BASIC), eq(2));
        verify(mudCharacterRepository).save(eq(ch));

        assertTrue(response.getFeedback().isPresent());
        assertEquals(question, response.getNext());
    }

    @Test
    void testAddTooManyNumber() {
        Long chId = random.nextLong();

        lenient().when(characterComponent.getBaseEffort(Effort.ULTIMATE)).thenReturn(STARTING_EFFORTS);
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("1+10"));

        verify(characterComponent, never()).addBaseEffort(any(Effort.class), anyInt());

        Output answer = response.getFeedback().orElseThrow();
        assertTrue(answer.getOutput().stream().anyMatch(line -> line.contains("[red]")));
        assertEquals(question, response.getNext());
    }

    @Test
    void testAddTooMany() {
        Long chId = random.nextLong();

        lenient().when(characterComponent.getBaseEffort(Effort.BASIC)).thenReturn(STARTING_EFFORTS);
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("1+"));

        verify(characterComponent, never()).addBaseEffort(any(Effort.class), anyInt());
        verify(mudCharacterRepository).save(eq(ch));

        Output answer = response.getFeedback().orElseThrow();
        assertTrue(answer.getOutput().stream().anyMatch(line -> line.contains("[red]")));
        assertEquals(question, response.getNext());
    }

    @Test
    void testInvalidAddition() {
        Long chId = random.nextLong();

        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("+"));

        verify(characterComponent, never()).addBaseEffort(any(Effort.class), anyInt());

        Output answer = response.getFeedback().orElseThrow();
        assertTrue(answer.getOutput().stream().anyMatch(line -> line.contains("[red]")));
        assertEquals(question, response.getNext());
    }

    @Test
    void testSubtract() {
        Long chId = random.nextLong();

        lenient().when(characterComponent.getBaseEffort(Effort.BASIC)).thenReturn(STARTING_EFFORTS);
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("1-"));

        verify(characterComponent).addBaseEffort(eq(Effort.BASIC), eq(-1));
        verify(mudCharacterRepository).save(eq(ch));

        assertTrue(response.getFeedback().isPresent());
        assertEquals(question, response.getNext());
    }

    @Test
    void testSubtractTooMany() {
        Long chId = random.nextLong();

        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("1-"));

        verify(characterComponent, never()).addBaseEffort(any(Effort.class), anyInt());

        Output answer = response.getFeedback().orElseThrow();
        assertTrue(answer.getOutput().stream().anyMatch(line -> line.contains("[red]")));
        assertEquals(question, response.getNext());
    }

    @Test
    void testInvalidSubtraction() {
        Long chId = random.nextLong();

        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("-"));

        verify(characterComponent, never()).addBaseEffort(any(Effort.class), anyInt());

        Output answer = response.getFeedback().orElseThrow();
        assertTrue(answer.getOutput().stream().anyMatch(line -> line.contains("[red]")));
        assertEquals(question, response.getNext());
    }

    @Test
    void testGarbageInput() {
        Long chId = random.nextLong();

        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("F"));

        verify(characterComponent, never()).addBaseEffort(any(Effort.class), anyInt());

        Output answer = response.getFeedback().orElseThrow();
        assertTrue(answer.getOutput().stream().anyMatch(line -> line.contains("[red]")));
        assertEquals(question, response.getNext());
    }

    @Test
    void testSave() {
        Long chId = random.nextLong();

        lenient().when(characterComponent.getBaseEffort(Effort.BASIC)).thenReturn(STARTING_EFFORTS);
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("s"));

        verify(mudCharacterRepository).save(eq(ch));

        assertTrue(response.getFeedback().isPresent());
        assertEquals(nextQuestion, response.getNext());
    }

    @Test
    void testSaveWhileUnallocated() {
        Long chId = random.nextLong();

        lenient().when(characterComponent.getBaseEffort(Effort.BASIC)).thenReturn(STARTING_EFFORTS - 1);
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        CharacterEffortQuestion uut = new CharacterEffortQuestion(applicationContext, repositoryBundle);
        Response response = uut.answer(wsContext, new Input("s"));

        verify(mudCharacterRepository).save(eq(ch));

        Output answer = response.getFeedback().orElseThrow();
        assertTrue(answer.getOutput().stream().anyMatch(line -> line.contains("allocate exactly")));
        assertEquals(question, response.getNext());
    }
}
