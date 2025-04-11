package com.agonyforge.mud.demo.cli.question.login;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.cli.Response;
import com.agonyforge.mud.core.service.SessionAttributeService;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.export.ImportExportService;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.constant.Pronoun;
import com.agonyforge.mud.demo.model.repository.*;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static com.agonyforge.mud.demo.cli.question.login.CharacterViewQuestion.START_ROOM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CharacterViewQuestionTest {
    @Mock
    private RepositoryBundle repositoryBundle;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private SessionAttributeService sessionAttributeService;

    @Mock
    private MudCharacterRepository characterRepository;

    @Mock
    private MudItemRepository itemRepository;

    @Mock
    private MudRoomRepository roomRepository;

    @Mock
    private MudSpeciesRepository speciesRepository;

    @Mock
    private MudProfessionRepository professionRepository;

    @Mock
    private CommService commService;

    @Mock
    private MudCharacter ch;

    @Mock
    private PlayerComponent playerComponent;

    @Mock
    private CharacterComponent characterComponent;

    @Mock
    private LocationComponent locationComponent;

    @Mock
    private MudSpecies species;

    @Mock
    private MudProfession profession;

    @Mock
    private MudRoom room;

    @Mock
    private Question question;

    @Mock
    private WebSocketContext wsContext;

    @Mock
    private ImportExportService importExportService;

    @Mock
    private Principal principal;

    @Captor
    private ArgumentCaptor<MudCharacter> characterCaptor;

    @Captor
    private ArgumentCaptor<Output> outputCaptor;

    private CharacterSheetFormatter characterSheetFormatter;

    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(characterRepository);
        lenient().when(repositoryBundle.getItemRepository()).thenReturn(itemRepository);
        lenient().when(repositoryBundle.getRoomRepository()).thenReturn(roomRepository);
        lenient().when(repositoryBundle.getSpeciesRepository()).thenReturn(speciesRepository);
        lenient().when(repositoryBundle.getProfessionRepository()).thenReturn(professionRepository);

        characterSheetFormatter = Mockito.spy(new CharacterSheetFormatter());
    }

    @Test
    void testPrompt() {
        Long chId = random.nextLong();
        String characterName = "Scion";
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(wsContext.getAttributes()).thenReturn(attributes);
        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(characterComponent.getName()).thenReturn(characterName);
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(characterComponent.getPronoun()).thenReturn(Pronoun.SHE);
        when(characterComponent.getSpecies()).thenReturn(species);
        when(characterComponent.getProfession()).thenReturn(profession);

        CharacterViewQuestion uut = new CharacterViewQuestion(applicationContext, repositoryBundle, commService, sessionAttributeService, characterSheetFormatter, importExportService);
        Output result = uut.prompt(wsContext);

        int i = 0;
        assertEquals(22, result.getOutput().size());
        assertTrue(result.getOutput().get(i++).contains("CHARACTER SHEET"));
        assertTrue(result.getOutput().get(i++).contains(characterName));
        assertTrue(result.getOutput().get(i++).contains(Pronoun.SHE.getObject()));
        assertTrue(result.getOutput().get(i++).contains("Species:"));
        assertTrue(result.getOutput().get(i++).contains("Profession:"));
        i++; // blank line
        assertTrue(result.getOutput().get(i).contains("Stats"));
        assertTrue(result.getOutput().get(i++).contains("Efforts"));
        assertTrue(result.getOutput().get(i).contains("Basic"));
        assertTrue(result.getOutput().get(i++).contains("STR:"));
        assertTrue(result.getOutput().get(i).contains("Weapons & Tools"));
        assertTrue(result.getOutput().get(i++).contains("DEX:"));
        assertTrue(result.getOutput().get(i).contains("Guns"));
        assertTrue(result.getOutput().get(i++).contains("CON:"));
        assertTrue(result.getOutput().get(i).contains("Energy & Magic"));
        assertTrue(result.getOutput().get(i++).contains("INT:"));
        assertTrue(result.getOutput().get(i).contains("Ultimate"));
        assertTrue(result.getOutput().get(i++).contains("WIS:"));
        assertTrue(result.getOutput().get(i++).contains("CHA:"));
        i++; // blank line
        assertTrue(result.getOutput().get(i++).contains("Health:"));
        assertTrue(result.getOutput().get(i++).contains("DEF:"));
        assertEquals("", result.getOutput().get(i++));
        assertTrue(result.getOutput().get(i++).contains("Play"));
        assertTrue(result.getOutput().get(i++).contains("Delete"));
        assertTrue(result.getOutput().get(i++).contains("Export"));
        assertTrue(result.getOutput().get(i++).contains("Go back"));
        assertTrue(result.getOutput().get(i).contains("selection"));
    }

    @Test
    void testPromptNoCharacter() {
        CharacterViewQuestion uut = new CharacterViewQuestion(applicationContext, repositoryBundle, commService, sessionAttributeService, characterSheetFormatter, importExportService);
        Output result = uut.prompt(wsContext);

        assertTrue(result.getOutput().get(0).contains("[red]"));
        assertTrue(result.getOutput().get(0).contains("error has been reported"));
    }

    @Test
    void testAnswerPlayExisting() {
        Long chId = random.nextLong();
        String wsSessionId = UUID.randomUUID().toString();
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(wsContext.getAttributes()).thenReturn(attributes);
        when(wsContext.getSessionId()).thenReturn(wsSessionId);

        when(ch.getPlayer()).thenReturn(playerComponent);
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(ch.getLocation())
            .thenReturn(null)
            .thenReturn(locationComponent);
        when(ch.getCharacter().getName()).thenReturn("Scion");
        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(characterRepository.save(any(MudCharacter.class))).thenAnswer(i -> i.getArguments()[0]);
        when(roomRepository.findById(eq(START_ROOM))).thenReturn(Optional.of(room));
        when(applicationContext.getBean(eq("commandQuestion"), eq(Question.class))).thenReturn(question);

        CharacterViewQuestion uut = new CharacterViewQuestion(applicationContext, repositoryBundle, commService, sessionAttributeService, characterSheetFormatter, importExportService);
        Response result = uut.answer(wsContext, new Input("p"));

        verify(characterRepository).save(characterCaptor.capture());
        verify(commService).sendToAll(any(WebSocketContext.class), outputCaptor.capture(), eq(ch));

        MudCharacter instance = characterCaptor.getValue();

        verify(ch).setLocation(any(LocationComponent.class));
        verify(instance.getLocation()).setRoom(eq(room));
        verify(playerComponent).setWebSocketSession(eq(wsSessionId));

        Output announcement = outputCaptor.getValue();

        assertTrue(announcement.getOutput().get(0).contains("has entered the game"));

        assertEquals(question, result.getNext());
    }

    @Test
    void testAnswerPlayNew() {
        Long chId = random.nextLong();
        String wsSessionId = UUID.randomUUID().toString();
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(wsContext.getAttributes()).thenReturn(attributes);
        when(wsContext.getSessionId()).thenReturn(wsSessionId);

        when(ch.getPlayer()).thenReturn(playerComponent);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(characterRepository.save(any(MudCharacter.class))).thenAnswer(i -> i.getArguments()[0]);
        when(roomRepository.findById(eq(START_ROOM))).thenReturn(Optional.of(room));
        when(applicationContext.getBean(eq("commandQuestion"), eq(Question.class))).thenReturn(question);

        CharacterViewQuestion uut = new CharacterViewQuestion(applicationContext, repositoryBundle, commService, sessionAttributeService, characterSheetFormatter, importExportService);
        Response result = uut.answer(wsContext, new Input("p"));

        verify(characterRepository).save(characterCaptor.capture());
        verify(commService).sendToAll(any(WebSocketContext.class), outputCaptor.capture(), eq(ch));

        MudCharacter instance = characterCaptor.getValue();

        verify(instance.getLocation()).setRoom(eq(room));
        verify(playerComponent).setWebSocketSession(eq(wsSessionId));

        Output announcement = outputCaptor.getValue();

        assertTrue(announcement.getOutput().get(0).contains("has entered the game"));

        assertEquals(question, result.getNext());
    }

    @Test
    void testAnswerDelete() {
        when(applicationContext.getBean(eq("characterDeleteQuestion"), eq(Question.class))).thenReturn(question);

        CharacterViewQuestion uut = new CharacterViewQuestion(applicationContext, repositoryBundle, commService, sessionAttributeService, characterSheetFormatter, importExportService);
        Response result = uut.answer(wsContext, new Input("d"));

        assertEquals(question, result.getNext());

        verify(characterRepository, never()).delete(any());
    }

    @Test
    void testAnswerBack() {
        when(applicationContext.getBean(eq("characterMenuQuestion"), eq(Question.class))).thenReturn(question);

        CharacterViewQuestion uut = new CharacterViewQuestion(applicationContext, repositoryBundle, commService, sessionAttributeService, characterSheetFormatter, importExportService);
        Response result = uut.answer(wsContext, new Input("b"));

        assertEquals(question, result.getNext());

        verify(characterRepository, never()).delete(any());
    }

    @Test
    void testAnswerExport() throws IOException {
        Long chId = random.nextLong();
        String principalName = String.valueOf(random.nextLong());
        String wsSessionId = UUID.randomUUID().toString();
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(wsContext.getAttributes()).thenReturn(attributes);
        lenient().when(wsContext.getSessionId()).thenReturn(wsSessionId);

        when(wsContext.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(principalName);

        lenient().when(ch.getPlayer()).thenReturn(playerComponent);
        lenient().when(ch.getCharacter()).thenReturn(characterComponent);
        lenient().when(ch.getLocation())
            .thenReturn(null)
            .thenReturn(locationComponent);
        lenient().when(ch.getCharacter().getName()).thenReturn("Scion");
        lenient().when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        lenient().when(characterRepository.save(any(MudCharacter.class))).thenAnswer(i -> i.getArguments()[0]);
        lenient().when(roomRepository.findById(eq(START_ROOM))).thenReturn(Optional.of(room));

        when(applicationContext.getBean(eq("characterViewQuestion"), eq(Question.class))).thenReturn(question);

        CharacterViewQuestion uut = new CharacterViewQuestion(applicationContext, repositoryBundle, commService, sessionAttributeService, characterSheetFormatter, importExportService);
        Response result = uut.answer(wsContext, new Input("e"));

        assertEquals(question, result.getNext());

        verify(importExportService).export(any(), any(), any());
        verify(characterRepository, never()).delete(any());


    }

    @Test
    void testAnswerUnknown() {
        Long chId = random.nextLong();
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(wsContext.getAttributes()).thenReturn(attributes);
        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));

        CharacterViewQuestion uut = new CharacterViewQuestion(applicationContext, repositoryBundle, commService, sessionAttributeService, characterSheetFormatter, importExportService);
        Response result = uut.answer(wsContext, new Input("x"));
        Output output = result.getFeedback().orElseThrow();

        assertEquals(uut, result.getNext());
        assertTrue(output.getOutput().get(0).contains("try again"));
    }
}
