package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.cli.question.login.CharacterMenuQuestion;
import com.agonyforge.mud.demo.model.constant.Pronoun;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.BannedUsersRepository;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.model.repository.UserRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BanCommandTest {

    private static final Random RANDOM = new Random();

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
    private MudCharacter ch, target;

    @Mock
    private CharacterComponent chCharacter, targetCharacter;

    @Mock
    private PlayerComponent chPlayer, targetPlayer;

    @Mock
    private MudCharacterRepository mudCharacterRepository;

    @Mock
    private MudRoom room, destination;

    @Mock
    private LocationComponent chLocation, targetLocation;

    @Mock
    private BannedUsersRepository bannedUsersRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User targetUser;

    @Mock
    private BannedUser targetBannedUserTemp, targetBannedUserPerm;

    private String targetPrincipal;

    private Principal principalInstance;

    @BeforeEach
    void setUp() {
        Long chId = RANDOM.nextLong();
        Long targetId = RANDOM.nextLong();
        targetPrincipal = String.valueOf(RANDOM.nextLong());

        lenient().when(target.getCreatedBy()).thenReturn(targetPrincipal);

        lenient().when(userRepository.findUserByPrincipalName(targetPrincipal)).thenReturn(Optional.of(targetUser));

        lenient().when(mudCharacterRepository.findAll()).thenReturn(List.of(ch, target));

        lenient().when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);
        lenient().when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        lenient().when(ch.getLocation()).thenReturn(chLocation);
        lenient().when(chLocation.getRoom()).thenReturn(destination);
        lenient().when(ch.getCharacter()).thenReturn(chCharacter);
        lenient().when(chCharacter.getName()).thenReturn("Scion");
        lenient().when(ch.getPlayer()).thenReturn(chPlayer);

        lenient().when(mudCharacterRepository.findById(eq(targetId))).thenReturn(Optional.of(target));
        lenient().when(target.getLocation()).thenReturn(targetLocation);
        lenient().when(targetLocation.getRoom()).thenReturn(room);
        lenient().when(target.getCharacter()).thenReturn(targetCharacter);
        lenient().when(targetCharacter.getName()).thenReturn("Target");
        lenient().when(targetCharacter.getPronoun()).thenReturn(Pronoun.THEY);
        lenient().when(target.getPlayer()).thenReturn(chPlayer);

        lenient().when(bannedUsersRepository.findById(1L)).thenReturn(Optional.of(targetBannedUserTemp));
        lenient().when(targetBannedUserTemp.getId()).thenReturn(1L);
        lenient().when(targetBannedUserTemp.isPermanent()).thenReturn(false);
        lenient().when(bannedUsersRepository.findById(2L)).thenReturn(Optional.of(targetBannedUserPerm));
        lenient().when(targetBannedUserPerm.getId()).thenReturn(2L);
        lenient().when(targetBannedUserPerm.isPermanent()).thenReturn(true);
    }

    // BAN add <user> PERM <důvod>
    // BAN add <user> TEMP <doba> <důvod>
    // BAN renew <ban_id> <doba>
    // BAN remove <ban_id>

    @Test
    void testNoArgs() {
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        Question result = uut.execute(question, wsContext, List.of("BAN"), new Input("ban"), output);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("What ban action would you like to take?")));

        assertEquals(question, result);

        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ban addr",
        "ban delete",
        "ban return",
        "ban make",
        "ban give",
        "ban addr!",
        "ban delete$",
        "ban return@",
        "ban make#",
        "ban give%",
        "ban ^give",
        "ban /delete",
        "ban ?give",
        "ban give;",
        "ban ~addr",
        "ban addr\u202E",
        "ban delete\u200C",
        "ban return\u200B",
        "ban make\u2060",
        "ban give\uFEFF",
        "ban ©addr",
        "ban •give",
        "ban −delete",
        "ban ─make",
        "ban \uD83D\uDE00give"
    })
    void testOneArgumentInvalidAction(String input) {
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(input.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(input), output);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Unknown ban action.")));

        assertEquals(question, result);

        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testOneArgumentAddNotValid() {
        String command = "ban add";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Who would you like to ban?")));

        assertEquals(question, result);

        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testOneArgumentRenewNotValid() {
        String command = "ban renew";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Whose ban would you like renew?")));

        assertEquals(question, result);

        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }
    @Test
    void testOneArgumentRemoveNotValid() {
        String command = "ban remove";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Who would you like to unban?")));


        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testOneArgumentBanListTempBan() throws InterruptedException {
        String command = "ban list";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        long userId = 100L;

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

        lenient().when(bannedUsersRepository.findAllByOrderByBannedOnAsc()).thenReturn(List.of(targetBannedUserTemp));

        lenient().when(targetBannedUserTemp.getReason()).thenReturn(reason);
        lenient().when(targetBannedUserTemp.getBannedOn()).thenReturn(dayBefore);
        lenient().when(targetBannedUserTemp.getBannedToDate()).thenReturn(nextDay);
        lenient().when(targetBannedUserTemp.getId()).thenReturn(userId);
        lenient().when(targetBannedUserTemp.isPermanent()).thenReturn(false);

        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        Optional<String> banListTitleOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains("Banned Users:"))
            .findFirst();

        assertTrue(banListTitleOptional.isPresent());

        Optional<String> bannedUserIdOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains(String.valueOf(userId)))
            .findFirst();

        assertTrue(bannedUserIdOptional.isPresent());

        Optional<String> TypeOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains("Temporary Ban"))
            .findFirst();

        assertTrue(TypeOptional.isPresent());

        String formattedStartDate = formatter.format(targetBannedUserTemp.getBannedOn());

        Optional<String> bannedOnDateOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains(formattedStartDate))
            .findFirst();

        assertTrue(bannedOnDateOptional.isPresent());

        String formattedBannedToDate = formatter.format(targetBannedUserTemp.getBannedToDate());

        Optional<String> bannedToDateOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains(formattedBannedToDate))
            .findFirst();

        assertTrue(bannedToDateOptional.isPresent());

        Optional<String> timeRemainingOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains(timeRemaining))
            .findFirst();

        assertTrue(timeRemainingOptional.isPresent());

    }

    @Test
    void testOneArgumentBanListPermBan() {
        String command = "ban list";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        long userId = 100L;

        String reason = "spam";

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date dayBefore = calendar.getTime();

        lenient().when(bannedUsersRepository.findAllByOrderByBannedOnAsc()).thenReturn(List.of(targetBannedUserPerm));

        lenient().when(targetBannedUserPerm.getReason()).thenReturn(reason);
        lenient().when(targetBannedUserPerm.getBannedOn()).thenReturn(dayBefore);
        lenient().when(targetBannedUserPerm.getId()).thenReturn(userId);
        lenient().when(targetBannedUserPerm.isPermanent()).thenReturn(true);

        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        Optional<String> banListTitleOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains("Banned Users:"))
            .findFirst();

        assertTrue(banListTitleOptional.isPresent());

        Optional<String> bannedUserIdOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains(String.valueOf(userId)))
            .findFirst();

        assertTrue(bannedUserIdOptional.isPresent());

        Optional<String> TypeOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains("Permanent Ban"))
            .findFirst();

        assertTrue(TypeOptional.isPresent());

        String formattedStartDate = formatter.format(targetBannedUserPerm.getBannedOn());

        Optional<String> bannedOnDateOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains(formattedStartDate))
            .findFirst();

        assertTrue(bannedOnDateOptional.isPresent());

        Optional<String> bannedToDateOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains("Banned Until:"))
            .findFirst();

        assertTrue(bannedToDateOptional.isEmpty());

        Optional<String> timeRemainingOptional = output.getOutput()
            .stream()
            .filter(line -> line.contains("Time remaining: "))
            .findFirst();

        assertTrue(timeRemainingOptional.isEmpty());

    }

    @Test
    void testTwoArgumentsAddNotValid() {
        String command = "ban add Target";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("You must specify a ban type.")));


        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testTwoArgumentsRenewNotValid() {
        String command = "ban renew 1";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("You must specify a ban renewal duration.")));


        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testPermBanNoReason() {
        String command = "ban add Target perm";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertFalse(output.getOutput().stream().anyMatch(line -> line.contains("Invalid time format.")));

        verify(bannedUsersRepository).save(any());

        verify(commService).reloadUser(targetPrincipal);

        verify(commService).sendToAll(eq(wsContext), any(Output.class), eq(ch));
    }

    @Test
    void testAddPermBanWithReason() {
        String command = "ban add Target perm spam";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertFalse(output.getOutput().stream().anyMatch(line -> line.contains("Invalid time format.")));

        verify(bannedUsersRepository).save(any());

        verify(commService).reloadUser(targetPrincipal);

        verify(commService).sendToAll(eq(wsContext), any(Output.class), eq(ch));
    }

    @Test
    void testAddTempBanNoLength() {
        String command = "ban add Target temp";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("You must specify a ban duration.")));

        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ban add Target temp 1x",
        "ban add Target temp 1d15m1h",
        "ban add Target temp 1h15m1d",
        "ban add Target temp 1h15d1m",
        "ban add Target temp 1m15h1d",
        "ban add Target temp 1m15d1h",
        "ban add Target temp 1h15h1h",
        "ban add Target temp 1d15d1d",
        "ban add Target temp 1m15m1m"
    })
    void testWrongDurationTempBan(String command) {
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Invalid time format.")));

        verifyNoInteractions(commService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ban add Target temp 1d1h15m",
        "ban add Target temp 1d1h",
        "ban add Target temp 1d15m",
        "ban add Target temp 1h1m",
        "ban add Target temp 1m",
    })
    void testAddTempBanNoReason(String command) {
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Target has been banned!")));

        verify(bannedUsersRepository).save(any());

        verify(commService).reloadUser(targetPrincipal);

        verify(commService).sendToAll(eq(wsContext), any(Output.class), eq(ch));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ban add Target temp 1d1h15m spam",
        "ban add Target temp 1d1h spam spam",
        "ban add Target temp 1d15m spam spam spam",
        "ban add Target temp 1h1m spam spam spam spam",
        "ban add Target temp 1m spam spam spam spam spam",
    })
    void testAddTempBanWithReason(String command) {
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Target has been banned!")));

        verify(bannedUsersRepository).save(any());

        verify(commService).reloadUser(targetPrincipal);

        ArgumentCaptor<BannedUser> userCaptor = ArgumentCaptor.forClass(BannedUser.class);
        verify(bannedUsersRepository).save(userCaptor.capture());

        BannedUser bannedUser = userCaptor.getValue();

        String[] words = command.split("\\s+");
        String reason = words.length < 6 ? "" : String.join(" ", Arrays.copyOfRange(words, 5, words.length));

        assertEquals(bannedUser.getReason(), reason);

        verify(commService).sendToAll(eq(wsContext), any(Output.class), eq(ch));
    }

    @Test
    void testRemoveBan() {
        String command = "ban remove 1";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Player with id 1 has been unbanned.")));

        verify(bannedUsersRepository).delete(targetBannedUserTemp);
    }

    @Test
    void testRenewTempBan() {
        String command = "ban renew 1 1d";

        BannedUser targetBannedUserTemp = spy(new BannedUser());

        Date date = new Date();
        targetBannedUserTemp.setBannedToDate(date);

        lenient().when(bannedUsersRepository.findById(1L)).thenReturn(Optional.of(targetBannedUserTemp));

        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date nextDay = calendar.getTime();

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Ban has been lengthened by 1d.")));
        assertEquals(nextDay, targetBannedUserTemp.getBannedToDate());

        verify(bannedUsersRepository).save(targetBannedUserTemp);

    }

    @Test
    void testRenewPermBan() {
        String command = "ban renew 2 1d";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Can't renew permanent ban.")));

        verifyNoInteractions(commService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testRenewNotExistingBan() {
        String command = "ban renew 3 1d";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Can't find player with that ban ID.")));

        verifyNoInteractions(commService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testRenewTempBanNoLength() {
        String command = "ban renew 1";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("You must specify a ban renewal duration.")));

        verifyNoInteractions(commService);
        verifyNoInteractions(bannedUsersRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void testRemoveNotExistingBan() {
        String command = "ban remove 3";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Can't find player with that ban ID.")));

        verifyNoInteractions(commService);
        verifyNoInteractions(userRepository);
        verify(bannedUsersRepository, never()).delete(any());
    }
}
