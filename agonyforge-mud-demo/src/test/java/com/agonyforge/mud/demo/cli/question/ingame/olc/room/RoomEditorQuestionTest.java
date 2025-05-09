package com.agonyforge.mud.demo.cli.question.ingame.olc.room;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.cli.Response;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.RoomFlag;
import com.agonyforge.mud.demo.model.impl.CharacterComponent;
import com.agonyforge.mud.demo.model.impl.LocationComponent;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.impl.MudRoom;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.model.repository.MudRoomRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static com.agonyforge.mud.demo.cli.question.ingame.olc.room.RoomEditorQuestion.REDIT_MODEL;
import static com.agonyforge.mud.demo.cli.question.ingame.olc.room.RoomEditorQuestion.REDIT_STATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomEditorQuestionTest {
    private static final Random RAND = new Random();

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private RepositoryBundle repositoryBundle;

    @Mock
    private MudCharacterRepository characterRepository;

    @Mock
    private CommService commService;

    @Mock
    private MudRoomRepository roomRepository;

    @Mock
    private WebSocketContext wsContext;

    @Mock
    private Question question;

    @Mock
    private MudCharacter ch;

    @Mock
    private CharacterComponent characterComponent;

    @Mock
    private LocationComponent locationComponent;

    @Mock
    private MudRoom room;

    @BeforeEach
    void setup() {
        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(characterRepository);
        lenient().when(repositoryBundle.getRoomRepository()).thenReturn(roomRepository);
    }

    @Test
    void testPrompt() {
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(room.getId()).thenReturn(roomId);
        when(room.getFlags()).thenReturn(EnumSet.noneOf(RoomFlag.class));
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Output output = uut.prompt(wsContext);

        assertTrue(output.getOutput().get(1).contains("*****"));
    }

    @Test
    void testPromptRoomTitle() {
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);
        attributes.put(REDIT_STATE, "ROOM.TITLE");

        when(room.getId()).thenReturn(roomId);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Output output = uut.prompt(wsContext);

        assertTrue(output.getOutput().get(0).contains("New title"));
    }

    @Test
    void testPromptRoomDescription() {
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);
        attributes.put(REDIT_STATE, "ROOM.DESCRIPTION");

        when(room.getId()).thenReturn(roomId);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Output output = uut.prompt(wsContext);

        assertTrue(output.getOutput().get(0).contains("New description"));
    }

    @Test
    void testAnswerExits() {
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(room.getId()).thenReturn(roomId);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));
        when(applicationContext.getBean(eq("roomExitsEditorQuestion"), eq(Question.class))).thenReturn(question);

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Response response = uut.answer(wsContext, new Input("e"));

        assertEquals(question, response.getNext());
        assertTrue(response.getFeedback().isPresent());
    }

    @Test
    void testAnswerRoomTitle() {
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(room.getId()).thenReturn(roomId);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));
        when(applicationContext.getBean(eq("roomEditorQuestion"), eq(Question.class))).thenReturn(question);

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Response response = uut.answer(wsContext, new Input("t"));

        assertEquals(question, response.getNext());
        assertTrue(response.getFeedback().isPresent());
        assertEquals("ROOM.TITLE", attributes.get(REDIT_STATE));
    }

    @Test
    void testAnswerRoomDescription() {
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(room.getId()).thenReturn(roomId);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));
        when(applicationContext.getBean(eq("roomEditorQuestion"), eq(Question.class))).thenReturn(question);

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Response response = uut.answer(wsContext, new Input("d"));

        assertEquals(question, response.getNext());
        assertTrue(response.getFeedback().isPresent());
        assertEquals("ROOM.DESCRIPTION", attributes.get(REDIT_STATE));
    }

    @Test
    void testAnswerSave() {
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);

        when(room.getId()).thenReturn(roomId);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(characterComponent.getName()).thenReturn("Name");
        when(ch.getCharacter()).thenReturn(characterComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));
        when(applicationContext.getBean(eq("commandQuestion"), eq(Question.class))).thenReturn(question);

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Response response = uut.answer(wsContext, new Input("x"));

        verify(roomRepository).save(eq(room));

        assertEquals(question, response.getNext());
        assertTrue(response.getFeedback().isPresent());
        assertFalse(attributes.containsKey(REDIT_STATE));
        assertFalse(attributes.containsKey(REDIT_MODEL));
    }

    @Test
    void testAnswerSetTitle() {
        String newTitle = "Fancy New Room Title";
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);
        attributes.put(REDIT_STATE, "ROOM.TITLE");

        when(room.getId()).thenReturn(roomId);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));
        when(applicationContext.getBean(eq("roomEditorQuestion"), eq(Question.class))).thenReturn(question);

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Response response = uut.answer(wsContext, new Input(newTitle));

        verify(room).setName(eq(newTitle));

        assertEquals(question, response.getNext());
        assertTrue(response.getFeedback().isPresent());
        assertFalse(attributes.containsKey(REDIT_STATE));
        assertTrue(attributes.containsKey(REDIT_MODEL));
    }

    @Test
    void testAnswerSetDescription() {
        String newDescription = """
            Once upon a time, in New York City in 1941... at this club open to all comers to play, 
            night after night, at a club named "Minston's Play House" in Harlem, they play jazz 
            sessions competing with each other. Young jazz men with a new sense are gathering. At 
            last they created a new genre itself. They are sick and tired of the conventional fixed 
            style jazz. They're eager to play jazz more freely as they wish then... in 2071 in the 
            universe... The bounty hunters, who are gathering in the spaceship "BEBOP", will play 
            freely without fear of risky things. They must create new dreams and films by breaking 
            traditional styles. The work, which becomes a new genre itself, will be called... 
            COWBOY BEBOP""";
        String expectedDesc = """
            Once upon a time, in New York City in 1941... at this club open to all comers to play,
            night after night, at a club named &quot;Minston&#39;s Play House&quot; in Harlem, they play jazz
            sessions competing with each other. Young jazz men with a new sense are gathering. At
            last they created a new genre itself. They are sick and tired of the conventional fixed
            style jazz. They&#39;re eager to play jazz more freely as they wish then... in 2071 in the
            universe... The bounty hunters, who are gathering in the spaceship &quot;BEBOP&quot;, will play
            freely without fear of risky things. They must create new dreams and films by breaking
            traditional styles. The work, which becomes a new genre itself, will be called...
            COWBOY BEBOP""";
        Long chId = RAND.nextLong();
        long roomId = RAND.nextLong(100, 200);
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(MUD_CHARACTER, chId);
        attributes.put(REDIT_STATE, "ROOM.DESCRIPTION");

        when(room.getId()).thenReturn(roomId);
        when(ch.getLocation()).thenReturn(locationComponent);
        when(ch.getLocation().getRoom()).thenReturn(room);
        when(wsContext.getAttributes()).thenReturn(attributes);

        when(characterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(room));
        when(applicationContext.getBean(eq("roomEditorQuestion"), eq(Question.class))).thenReturn(question);

        RoomEditorQuestion uut = new RoomEditorQuestion(applicationContext, repositoryBundle, commService);
        Response response = uut.answer(wsContext, new Input(newDescription));

        verify(room).setDescription(eq(expectedDesc));

        assertEquals(question, response.getNext());
        assertTrue(response.getFeedback().isPresent());
        assertFalse(attributes.containsKey(REDIT_STATE));
        assertTrue(attributes.containsKey(REDIT_MODEL));
    }
}
