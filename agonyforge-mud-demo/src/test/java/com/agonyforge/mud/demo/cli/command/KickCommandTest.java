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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KickCommandTest {
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


    @Test
    void testKickNoArgs() {
        Output output = new Output();
        KickCommand uut = new KickCommand(repositoryBundle, commService, applicationContext, userRepository, reloadedUsersRepository);

        Question result = uut.execute(question, wsContext, List.of("KICK"), new Input("kick"), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Whom would you like to kick?")));

        verifyNoInteractions(commService);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(reloadedUsersRepository);
    }


    @Test
    void testKickValidUser() {

        Output output = new Output();
        KickCommand uut = new KickCommand(repositoryBundle, commService, applicationContext, userRepository, reloadedUsersRepository);

        Question result = uut.execute(question, wsContext, List.of("KICK", "TARGET"), new Input("kick Target"), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Target has been kicked!")));

        verify(reloadedUsersRepository).save(any());

        verify(commService).reloadUser(targetPrincipal);

        verify(commService).sendToAll(eq(wsContext), any(Output.class), eq(ch));

    }

    @Test
    void testKickInvalidUser() {

        Output output = new Output();
        KickCommand uut = new KickCommand(repositoryBundle, commService, applicationContext, userRepository, reloadedUsersRepository);

        Question result = uut.execute(question, wsContext, List.of("KICK", "CARMEN"), new Input("kick Carmen"), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Can't find that player.")));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(reloadedUsersRepository);

        verify(commService, never()).reloadUser(targetPrincipal);

        verify(commService, never()).sendToAll(eq(wsContext), any(Output.class), eq(ch));

    }
}
