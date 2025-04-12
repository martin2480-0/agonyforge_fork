package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.export.ImportExportService;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
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

import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExportCommandTest {

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
    private Principal principal;

    @Mock
    private ImportExportService importExportService;

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
    void testExportNoArgs() {
        Output output = new Output();
        ExportCommand uut = new ExportCommand(repositoryBundle, commService, applicationContext, importExportService);

        Question result = uut.execute(question, wsContext, List.of("EXPORT"), new Input("export"), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("What would you like to export?")));

        verifyNoInteractions(commService);
        verifyNoInteractions(importExportService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "export character",
        "export items",
        "export map"
    })
    void testExportValidType(String command) throws IOException {

        Output output = new Output();
        ExportCommand uut = new ExportCommand(repositoryBundle, commService, applicationContext, importExportService);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);

        verify(importExportService).export(eq(tokens.get(1).toLowerCase()),any());

        verify(commService).triggerDownload(userPrincipal);



    }


    @Test
    void testExportIOException() throws IOException {
        Output output = new Output();
        ExportCommand uut = new ExportCommand(repositoryBundle, commService, applicationContext, importExportService);

        doThrow(new IOException()).when(importExportService).export("items", ch);

        Question result = uut.execute(question, wsContext, List.of("EXPORT", "ITEMS"), new Input("export items"), output);

        assertEquals(question, result);

        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains("Export error!")));

        verifyNoInteractions(commService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "export characters",
        "export item",
        "export maps",
        "export npcs"
    })
    void testExportInvalidType(String command) {

        Output output = new Output();
        ExportCommand uut = new ExportCommand(repositoryBundle, commService, applicationContext, importExportService);

        List<String> tokens = Arrays.asList(command.toUpperCase().split(" "));

        Question result = uut.execute(question, wsContext, tokens, new Input(command), output);

        assertEquals(question, result);
        assertTrue(output.getOutput().stream().anyMatch(line -> line.contains(String.format("Export of %s is not supported!", tokens.get(1).toLowerCase()))));

        verifyNoInteractions(importExportService);

    }
}
