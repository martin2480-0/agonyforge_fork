package com.agonyforge.mud.demo.cli.question.login;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.cli.Response;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.BannedUser;
import com.agonyforge.mud.demo.model.impl.CharacterComponent;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.impl.ReloadedUser;
import com.agonyforge.mud.demo.model.repository.BannedUsersRepository;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.model.repository.MudItemRepository;
import com.agonyforge.mud.demo.model.repository.ReloadedUsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CharacterMenuQuestionTest {
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
    private MudCharacter mudCharacter;

    @Mock
    private CharacterComponent characterComponent;

    @Mock
    private WebSocketContext webSocketContext;

    @Mock
    BannedUsersRepository bannedUsersRepository;

    @Mock
    ReloadedUsersRepository reloadedUsersRepository;

    @Mock
    ReloadedUser reloadedUser;

    @Mock
    BannedUser bannedUser;

    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(characterRepository);
        lenient().when(repositoryBundle.getItemRepository()).thenReturn(itemRepository);
    }

    @Test
    void testReloadReason(){
        String principalName = "principal";
        String reason = "spam";

        when(webSocketContext.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(principalName);
        lenient().when(reloadedUsersRepository.findByReloadedUser_PrincipalName(principalName)).thenReturn(Optional.of(reloadedUser));
        lenient().when(reloadedUser.getReason()).thenReturn(reason);


        CharacterMenuQuestion uut = new CharacterMenuQuestion(
            applicationContext,
            repositoryBundle,
            bannedUsersRepository,
            reloadedUsersRepository);

        Output result = uut.prompt(webSocketContext);

        Optional<String> itemOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains(reason))
            .findFirst();

        assertTrue(itemOptional.isPresent());
        verify(reloadedUsersRepository).delete(reloadedUser);
    }

    @Test
    void testBannedUserOneDay() throws InterruptedException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        String principalName = "principal";
        String reason = "spam";
        String timeRemaining = "Time remaining: 1 day 5 hours";

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date now = new Date();

        Thread.sleep(10);

        Date nextDay = new Date(now.getTime() + TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(1));

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date dayBefore = calendar.getTime();

        when(webSocketContext.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(principalName);
        lenient().when(bannedUsersRepository.findByBannedUser_PrincipalName(principalName)).thenReturn(Optional.of(bannedUser));

        lenient().when(bannedUser.getReason()).thenReturn(reason);
        lenient().when(bannedUser.getBannedOn()).thenReturn(dayBefore);
        lenient().when(bannedUser.getBannedToDate()).thenReturn(nextDay);

        CharacterMenuQuestion uut = new CharacterMenuQuestion(
            applicationContext,
            repositoryBundle,
            bannedUsersRepository,
            reloadedUsersRepository);

        Output result = uut.prompt(webSocketContext);

        Optional<String> bannedTitleOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains("You have been banned!"))
            .findFirst();

        assertTrue(bannedTitleOptional.isPresent());

        Optional<String> TypeOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains("temporary") || line.contains("permanent"))
            .findFirst();

        assertTrue(TypeOptional.isPresent());

        String formattedStartDate = formatter.format(bannedUser.getBannedOn());

        Optional<String> bannedOnDateOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains(formattedStartDate))
            .findFirst();

        assertTrue(bannedOnDateOptional.isPresent());

        String formattedBannedToDate = formatter.format(bannedUser.getBannedToDate());

        Optional<String> bannedToDateOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains(formattedBannedToDate))
            .findFirst();

        assertTrue(bannedToDateOptional.isPresent());

        Optional<String> timeRemainingOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains(timeRemaining))
            .findFirst();

        assertTrue(timeRemainingOptional.isPresent());

        verify(reloadedUsersRepository, never()).delete(any());


    }

    @Test
    void testPromptNoCharacters() {
        String principalName = "principal";

        when(principal.getName()).thenReturn(principalName);
        when(webSocketContext.getPrincipal()).thenReturn(principal);

        CharacterMenuQuestion uut = new CharacterMenuQuestion(
            applicationContext,
            repositoryBundle,
            bannedUsersRepository,
            reloadedUsersRepository);
        Output result = uut.prompt(webSocketContext);
        Optional<String> itemOptional = result.getOutput()
                .stream()
                .filter(line -> line.contains("New Character"))
                .findFirst();

        assertEquals(7, result.getOutput().size());
        assertTrue(itemOptional.isPresent());

        verify(characterRepository).findByPlayerUsername(eq(principalName));
    }

    @Test
    void testPromptWithCharacters() {
        String principalName = "principal";
        String characterName = "Scion";

        when(principal.getName()).thenReturn(principalName);
        when(webSocketContext.getPrincipal()).thenReturn(principal);
        when(characterComponent.getName()).thenReturn(characterName);
        when(mudCharacter.getCharacter()).thenReturn(characterComponent);
        when(characterRepository.findByPlayerUsername(eq(principalName))).thenReturn(List.of(mudCharacter));

        CharacterMenuQuestion uut = new CharacterMenuQuestion(
            applicationContext,
            repositoryBundle,
            bannedUsersRepository,
            reloadedUsersRepository);
        Output result = uut.prompt(webSocketContext);
        Optional<String> newCharacterLineOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains("New Character"))
            .findAny();
        Optional<String> characterNameLineOptional = result.getOutput()
            .stream()
            .filter(line -> line.contains(characterName) && line.contains("1"))
            .findAny();

        assertEquals(8, result.getOutput().size());
        assertTrue(newCharacterLineOptional.isPresent());
        assertTrue(characterNameLineOptional.isPresent());

        verify(characterRepository).findByPlayerUsername(eq(principalName));
    }

    @Test
    void testPromptTwice() {
        String principalName = "principal";

        when(principal.getName()).thenReturn(principalName);
        when(webSocketContext.getPrincipal()).thenReturn(principal);

        CharacterMenuQuestion uut = new CharacterMenuQuestion(
            applicationContext,
            repositoryBundle,
            bannedUsersRepository,
            reloadedUsersRepository);
        uut.prompt(webSocketContext);
        Output result = uut.prompt(webSocketContext);

        List<String> lines = result.getOutput()
            .stream()
            .filter(line -> line.contains("New Character"))
            .toList();

        assertEquals(1, lines.size());
    }

    @Test
    void testAnswerEmpty() {
        CharacterMenuQuestion uut = new CharacterMenuQuestion(
            applicationContext,
            repositoryBundle,
            bannedUsersRepository,
            reloadedUsersRepository);

        when(webSocketContext.getPrincipal()).thenReturn(principal);
        when(applicationContext.getBean(eq("characterMenuQuestion"), eq(Question.class))).thenReturn(uut);

        Response result = uut.answer(webSocketContext, new Input(""));

        assertEquals(uut, result.getNext());
        verify(webSocketContext, never()).getAttributes();
    }

    @Test
    void testAnswerNew() {
        when(webSocketContext.getPrincipal()).thenReturn(principal);
        when(applicationContext.getBean(eq("characterNameQuestion"), eq(Question.class))).thenReturn(question);

        CharacterMenuQuestion uut = new CharacterMenuQuestion(
            applicationContext,
            repositoryBundle,
            bannedUsersRepository,
            reloadedUsersRepository);
        Response result = uut.answer(webSocketContext, new Input("n"));

        assertEquals(question, result.getNext());

        verify(webSocketContext, never()).getAttributes();
    }

    @Test
    void testAnswerExisting() {
        String principalName = "principal";
        Long characterId = random.nextLong();
        String characterName = "Scion";
        Map<String, Object> attributes = new HashMap<>();

        when(principal.getName()).thenReturn(principalName);
        when(webSocketContext.getPrincipal()).thenReturn(principal);
        when(webSocketContext.getAttributes()).thenReturn(attributes);
        when(mudCharacter.getId()).thenReturn(characterId);
        when(mudCharacter.getCharacter()).thenReturn(characterComponent);
        when(characterComponent.getName()).thenReturn(characterName);
        when(characterRepository.findByPlayerUsername(eq(principalName))).thenReturn(List.of(mudCharacter));
        when(applicationContext.getBean(eq("characterViewQuestion"), eq(Question.class))).thenReturn(question);

        CharacterMenuQuestion uut = new CharacterMenuQuestion(
            applicationContext,
            repositoryBundle,
            bannedUsersRepository,
            reloadedUsersRepository);

        Response result = uut.answer(webSocketContext, new Input("1"));

        assertEquals(question, result.getNext());
        assertEquals(characterId, attributes.get(MUD_CHARACTER));
    }
}
