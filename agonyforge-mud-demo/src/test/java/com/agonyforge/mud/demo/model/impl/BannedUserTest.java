package com.agonyforge.mud.demo.model.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class BannedUserTest {

    @Test
    void testBannedUser() {
        BannedUser uut = new BannedUser();

        User u = new User();

        uut.setBannedUser(u);

        assertEquals(u, uut.getBannedUser());
    }

    @Test
    void testPermanent() {
        BannedUser uut = new BannedUser();

        uut.setPermanent(true);

        assertTrue(uut.isPermanent());
    }

    @Test
    void testReason() {
        BannedUser uut = new BannedUser();

        uut.setReason("spam");

        assertEquals("spam", uut.getReason());
    }

    @Test
    void testBannedToDate() {
        BannedUser uut = new BannedUser();

        Date bannedToDate = new Date();

        uut.setBannedToDate(bannedToDate);

        assertEquals(uut.getBannedToDate(), bannedToDate);
    }

    @Test
    void testBannedOn() {
        BannedUser uut = new BannedUser();

        Date bannedOn = new Date();

        uut.setBannedOn(bannedOn);

        assertEquals(uut.getBannedOn(), bannedOn);
    }

}
