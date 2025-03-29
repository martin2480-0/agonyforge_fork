package com.agonyforge.mud.demo.model.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CommandReferenceTest {
    @Test
    void testName() {
        CommandReference uut = new CommandReference();

        uut.setName("test");

        assertEquals("test", uut.getName());
    }

    @Test
    void testPriority() {
        CommandReference uut = new CommandReference();

        uut.setPriority(100);

        assertEquals(100, uut.getPriority());
    }

    @Test
    void testBeanName() {
        CommandReference uut = new CommandReference();

        uut.setBeanName("beanName");

        assertEquals("beanName", uut.getBeanName());
    }

    @Test
    void testCanBeForced() {
        CommandReference uut = new CommandReference();

        uut.setCanBeForced(true);

        assertTrue(uut.isCanBeForced());
    }

    @Test
    void testExecuteWhileFrozen() {
        CommandReference uut = new CommandReference();

        uut.setCanExecuteWhileFrozen(true);

        assertTrue(uut.isCanExecuteWhileFrozen());
    }
}
