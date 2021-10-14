package ru.job4j.grabber;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppTest {

    @Test
    public void triggerTest() {
        assertEquals(1, App.triggerTest());
    }
}