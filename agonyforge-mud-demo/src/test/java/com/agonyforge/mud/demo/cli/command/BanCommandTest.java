package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.repository.BannedUsersRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@Disabled
@ExtendWith(MockitoExtension.class)
public class BanCommandTest {

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
    private BannedUsersRepository bannedUsersRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

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

        assertEquals(question, result);

        verifyNoInteractions(commService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ban add",
        "ban renew",
        "ban remove",
    })
    void testOneArgumentNonValid(String input) {

    }

    @Test
    void testOneArgumentBanList() {
        String arg = "ban list";
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ban add Jeff",
        "ban renew Jeff",
    })
    void testTwoArgumentsNonValid(String input) {

    }

    @Test
    void testRemoveBan() {
        String arg = "ban remove 1";
    }

    @Test
    void testPermBanNoReason() {
        String arg = "ban add Jeff perm";
    }

    @Test
    void testAddPermBanWithReason() {
        String arg = "ban add Jeff perm spam";
    }

    @Test
    void testAddTempBanNoLength() {
        String arg = "ban add Jeff temp";
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ban add Jeff temp 1x",
        "ban add Jeff temp 1d15m1h",
        "ban add Jeff temp 1h15m1d",
        "ban add Jeff temp 1h15d1m",
        "ban add Jeff temp 1m15h1d",
        "ban add Jeff temp 1m15d1h",
        "ban add Jeff temp 1h15h1h",
        "ban add Jeff temp 1d15d1d",
        "ban add Jeff temp 1m15m1m"
    })
    void testWrongDurationTempBan(String input) {

    }

    @Test
    void testAddTempBan() {
        String arg = "ban add Jeff temp 1d";
    }

    @Test
    void testAddTempBanNoReason() {
        String arg = "ban add Jeff temp 1d";
    }

    @Test
    void testAddTempBanWithReason() {
        String arg = "ban add Jeff temp 1d spam";
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
