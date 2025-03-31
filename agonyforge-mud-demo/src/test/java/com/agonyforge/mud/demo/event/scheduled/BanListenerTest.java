package com.agonyforge.mud.demo.event.scheduled;

import com.agonyforge.mud.demo.model.impl.BannedUser;
import com.agonyforge.mud.demo.model.repository.BannedUsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BanListenerTest {

    @Mock
    private BannedUsersRepository bannedUsersRepository;

    @Mock
    private BannedUser expiredBan, activeBan;

    @InjectMocks
    private BanListener banListener;

    @Test
    void testCheckBannedUsers() {
        Date pastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60);
        Date futureDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60);

        when(expiredBan.getBannedToDate()).thenReturn(pastDate);
        when(activeBan.getBannedToDate()).thenReturn(futureDate);

        List<BannedUser> bannedUsers = Arrays.asList(expiredBan, activeBan);

        when(bannedUsersRepository.findByPermanentFalse()).thenReturn(bannedUsers);

        banListener.checkBannedUsers();

        verify(bannedUsersRepository).deleteAll(List.of(expiredBan));

    }
}
