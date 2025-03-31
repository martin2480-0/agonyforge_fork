package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.Pronoun;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.model.repository.ReloadedUsersRepository;
import com.agonyforge.mud.demo.model.repository.UserRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class SlayCommandTest {
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
    private User targetUser;

    @Mock
    private MudCharacterRepository mudCharacterRepository;

    @Mock
    private MudRoom room, destination;

    @Mock
    private LocationComponent chLocation, targetLocation;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReloadedUsersRepository reloadedUsersRepository;

    private static String targetPrincipal;

    @BeforeAll
    static void setupBeforeAll() {
        targetPrincipal = String.valueOf(RANDOM.nextLong());
    }

    @BeforeEach
    void setUp() {
        Long chId = RANDOM.nextLong();
        Long targetId = RANDOM.nextLong();

        lenient().when(mudCharacterRepository.findAll()).thenReturn(List.of(ch, target));


        lenient().when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);

        lenient().when(userRepository.findUserByPrincipalName(targetPrincipal)).thenReturn(Optional.of(targetUser));

        lenient().when(target.getCreatedBy()).thenReturn(targetPrincipal);

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);
        lenient().when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        lenient().when(ch.getLocation()).thenReturn(chLocation);
        lenient().when(chLocation.getRoom()).thenReturn(destination);
        lenient().when(ch.getCharacter()).thenReturn(chCharacter);
        lenient().when(chCharacter.getPronoun()).thenReturn(Pronoun.XE);
        lenient().when(chCharacter.getName()).thenReturn("Scion");
        lenient().when(ch.getPlayer()).thenReturn(chPlayer);
        when(ch.getLocation().getRoom()).thenReturn(room);

        lenient().when(mudCharacterRepository.findById(eq(targetId))).thenReturn(Optional.of(target));
        lenient().when(target.getLocation()).thenReturn(targetLocation);
        lenient().when(targetLocation.getRoom()).thenReturn(room);
        lenient().when(target.getCharacter()).thenReturn(targetCharacter);
        lenient().when(targetCharacter.getName()).thenReturn("Target");
        lenient().when(targetCharacter.getPronoun()).thenReturn(Pronoun.THEY);
        lenient().when(target.getPlayer()).thenReturn(chPlayer);
        lenient().when(target.getLocation().getRoom()).thenReturn(room);

        lenient().when(mudCharacterRepository.findByLocationRoom(eq(room))).thenReturn(List.of(target, ch));

    }

    @Test
    void testSlayNoArgs() {
        Output output = new Output();
        SlayCommand uut = new SlayCommand(repositoryBundle, commService, applicationContext, userRepository, reloadedUsersRepository);

        Question result = uut.execute(question, wsContext, List.of("SLAY"), new Input("slay"), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Whom do you wish to slay?")));

        verifyNoInteractions(commService);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(reloadedUsersRepository);
    }

    @Test
    void testSlayValidUser() {
        Output output = new Output();
        SlayCommand uut = new SlayCommand(repositoryBundle, commService, applicationContext, userRepository, reloadedUsersRepository);

        Question result = uut.execute(question, wsContext, List.of("SLAY", "TARGET"), new Input("slay Target"), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("You snap your fingers, and Target disappears!")));

        verify(reloadedUsersRepository).save(any());

        verify(commService).reloadUser(targetPrincipal);

        verify(commService).sendToRoom(any(), any(Output.class), eq(ch));

    }

    @Test
    void testSlayInvalidUser() {

        Output output = new Output();
        SlayCommand uut = new SlayCommand(repositoryBundle, commService, applicationContext, userRepository, reloadedUsersRepository);

        Question result = uut.execute(question, wsContext, List.of("SLAY", "CARMEN"), new Input("slay Carmen"), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("You don't see anyone like that.")));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(reloadedUsersRepository);

        verify(commService, never()).reloadUser(targetPrincipal);

        verify(commService, never()).sendToRoom(any(), any(Output.class), eq(ch));

    }

}
