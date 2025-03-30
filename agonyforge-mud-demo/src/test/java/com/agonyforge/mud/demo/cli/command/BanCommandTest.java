package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.Pronoun;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.BannedUsersRepository;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.model.repository.UserRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @BeforeEach
    void setUp() {
        Long chId = RANDOM.nextLong();
        Long targetId = RANDOM.nextLong();

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
        "ban \u00A9addr",
        "ban \u2022give",
        "ban \u2212delete",
        "ban \u2500make",
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
    void testOneArgumentBanList() {
        String arg = "ban list";
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
    void testRemoveBan() {
        String arg = "ban remove 1";
    }

    @Test
    void testPermBanNoReason() {
        String arg = "ban add Target perm";
    }

    @Test
    void testAddPermBanWithReason() {
        String arg = "ban add Target perm spam";
    }

    @Test
    void testAddTempBanNoLength() {
        String command = "ban add Target temp";
        Output output = new Output();
        BanCommand uut = new BanCommand(repositoryBundle, commService, applicationContext, bannedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);
        // TODO fix Target not found error
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
    void testWrongDurationTempBan(String input) {

    }

    @Test
    void testAddTempBan() {
        String arg = "ban add Target temp 1d";
    }

    @Test
    void testAddTempBanNoReason() {
        String arg = "ban add Target temp 1d";
    }

    @Test
    void testAddTempBanWithReason() {
        String arg = "ban add Target temp 1d spam";
    }

    @Test
    void testRenewTempBanNoLength() {
        String arg = "ban renew 1";
    }

    @Test
    void testRenewTempBan() {
        String arg = "ban renew 1 1d";
    }

    @Test
    void testRenewPermBan() {
        String arg = "ban renew 2 1d";
    }


}
