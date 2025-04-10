package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.model.repository.ReloadedUsersRepository;
import com.agonyforge.mud.demo.model.repository.UserRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.security.Principal;
import java.util.*;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportCommandTest {

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
    private MudCharacter ch;

    @Mock
    private CharacterComponent chCharacter;

    @Mock
    private PlayerComponent chPlayer;

    @Mock
    private User targetUser;

    @Mock
    private MudRoom destination;

    @Mock
    private LocationComponent chLocation;

    @Mock
    private MudCharacterRepository mudCharacterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReloadedUsersRepository reloadedUsersRepository;

    @Mock
    private Principal principal;

    private static String userPrincipal;


    @BeforeAll
    static void setupBeforeAll() {
        userPrincipal = String.valueOf(RANDOM.nextLong());
    }

    @BeforeEach
    void setUp() {
        Long chId = RANDOM.nextLong();

        lenient().when(mudCharacterRepository.findAll()).thenReturn(List.of(ch));

        lenient().when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        lenient().when(wsContext.getPrincipal()).thenReturn(principal);

        lenient().when(principal.getName()).thenReturn(userPrincipal);

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);

        lenient().when(userRepository.findUserByPrincipalName(userPrincipal)).thenReturn(Optional.of(targetUser));

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);
        lenient().when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        lenient().when(ch.getLocation()).thenReturn(chLocation);
        lenient().when(chLocation.getRoom()).thenReturn(destination);
        lenient().when(ch.getCharacter()).thenReturn(chCharacter);
        lenient().when(chCharacter.getName()).thenReturn("Scion");
        lenient().when(ch.getPlayer()).thenReturn(chPlayer);
        lenient().when(ch.getCreatedBy()).thenReturn(userPrincipal);
    }


    @Test
    void testImportNoArgs() {
        Output output = new Output();
        ImportCommand uut = new ImportCommand(repositoryBundle, commService, applicationContext, reloadedUsersRepository, userRepository);

        Question result = uut.execute(question, wsContext, List.of("IMPORT"), new Input("import"), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("What would you like to import?")));

        verifyNoInteractions(commService);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(reloadedUsersRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "import character",
        "import items",
        "import map"
    })
    void testImportValidType(String command) {

        Output output = new Output();
        ImportCommand uut = new ImportCommand(repositoryBundle, commService, applicationContext, reloadedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        verify(commService).triggerUpload(eq(userPrincipal), eq(tokens.get(1).toLowerCase()));

        verify(reloadedUsersRepository).save(any());

        verify(commService).reloadUser(userPrincipal);


    }

    @ParameterizedTest
    @ValueSource(strings = {
        "import characters",
        "import item",
        "import maps",
        "import npcs"
    })
    void testImportInvalidType(String command) {

        Output output = new Output();
        ImportCommand uut = new ImportCommand(repositoryBundle, commService, applicationContext, reloadedUsersRepository, userRepository);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains(String.format("Import of %s is not supported!", tokens.get(1).toLowerCase()))));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(reloadedUsersRepository);

        verify(commService, never()).reloadUser(userPrincipal);

    }
}
