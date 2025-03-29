package com.agonyforge.mud.demo.model.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ReloadedUserTest {

    @Test
    void testBannedUser() {
        ReloadedUser uut = new ReloadedUser();

        User u = new User();

        uut.setReloadedUser(u);

        assertEquals(u, uut.getReloadedUser());
    }

    @Test
    void testReason() {
        ReloadedUser uut = new ReloadedUser();

        String reason = "You have been kicked!";

        uut.setReason(reason);

        assertEquals(reason, uut.getReason());
    }

}
