package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.Pronoun;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.CommandRepository;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ForceCommandTest {
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
    private CommandRepository commandRepository;

    @Mock
    private CommandReference commandReference;


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
        lenient().when(chCharacter.getPronoun()).thenReturn(Pronoun.XE);
        lenient().when(ch.getPlayer()).thenReturn(chPlayer);

        lenient().when(mudCharacterRepository.findById(eq(targetId))).thenReturn(Optional.of(target));
        lenient().when(target.getLocation()).thenReturn(targetLocation);
        lenient().when(targetLocation.getRoom()).thenReturn(room);
        lenient().when(target.getCharacter()).thenReturn(targetCharacter);
        lenient().when(targetCharacter.getName()).thenReturn("Target");
        lenient().when(targetCharacter.getPronoun()).thenReturn(Pronoun.THEY);
        lenient().when(target.getPlayer()).thenReturn(targetPlayer);

        lenient().when(mudCharacterRepository.findAll()).thenReturn(List.of(ch, target));

    }

    // FORCE <user> <command> args of command

    @Test
    void testNoArgs() {
        Output output = new Output();
        ForceCommand uut = new ForceCommand(repositoryBundle, commService, applicationContext, commandRepository);

        Question result = uut.execute(question, wsContext, List.of("FOR"), new Input("for"), output);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Whom would you like to force to do something?")));

        assertEquals(question, result);

        verifyNoInteractions(commandRepository);
        verifyNoInteractions(commService);
    }

    @Test
    void testOneArg() {
        String command = "force Target";
        Output output = new Output();

        ForceCommand uut = new ForceCommand(repositoryBundle, commService, applicationContext, commandRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("What would you like to force them to do?")));

        verifyNoInteractions(commandRepository);
        verifyNoInteractions(commService);
    }

    @Test
    void testNonExistentUser() {
        String command = "force Carmen";
        Output output = new Output();

        ForceCommand uut = new ForceCommand(repositoryBundle, commService, applicationContext, commandRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Can't find that player.")));

        verifyNoInteractions(commandRepository);
        verifyNoInteractions(commService);
    }

    @Test
    void testFrozenUser(){
        when(commandRepository.findByNameIgnoreCase("up")).thenReturn(Optional.of(commandReference));
        when(commandReference.isCanBeForced()).thenReturn(true);
        when(commandReference.isCanExecuteWhileFrozen()).thenReturn(false);
        when(target.isFrozen()).thenReturn(true);

        String command = "force Target up";

        Output output = new Output();

        ForceCommand uut = new ForceCommand(repositoryBundle, commService, applicationContext, commandRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Can't execute this command, Target is frozen.")));
    }

    @Test
    @Disabled
    void testSupportedCommand() {
        when(commandRepository.findByNameIgnoreCase("who")).thenReturn(Optional.of(commandReference)); // TODO use better command
        when(commandReference.isCanBeForced()).thenReturn(true);

        String command = "force Target who";
        Output output = new Output();

        ForceCommand uut = new ForceCommand(repositoryBundle, commService, applicationContext, commandRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

    }

    @Test
    void testForceUnsupportedCommand() {
        when(commandRepository.findByNameIgnoreCase("quit")).thenReturn(Optional.of(commandReference));
        when(commandReference.isCanBeForced()).thenReturn(false);

        String command = "force Target quit";
        Output output = new Output();

        ForceCommand uut = new ForceCommand(repositoryBundle, commService, applicationContext, commandRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Can't force Target to quit")));

        verifyNoInteractions(commService);

    }

    @Test
    void testForceUnknownCommand() {
        String command = "force Target quick";
        Output output = new Output();

        ForceCommand uut = new ForceCommand(repositoryBundle, commService, applicationContext, commandRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Can't find that command.")));

        verifyNoInteractions(commService);

    }

}
