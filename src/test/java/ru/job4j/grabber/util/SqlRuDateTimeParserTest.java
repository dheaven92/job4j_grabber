package ru.job4j.grabber.util;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.Assert.assertEquals;

public class SqlRuDateTimeParserTest {

    @Test
    public void whenToday() {
        var today = LocalDateTime.now();
        assertEquals(
                LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 12, 34),
                new SqlRuDateTimeParser().parse("сегодня, 12:34")
        );
    }

    @Test
    public void whenYesterday() {
        var yesterday = LocalDateTime.now().minusDays(1);
        assertEquals(
                LocalDateTime.of(yesterday.getYear(), yesterday.getMonthValue(),
                        yesterday.getDayOfMonth(), 9, 0),
                new SqlRuDateTimeParser().parse("вчера, 09:00")
        );
    }

    @Test
    public void whenRegularDate() {
        DateTimeParser parser = new SqlRuDateTimeParser();
        assertEquals(
                LocalDateTime.of(2021, Month.OCTOBER, 8, 10, 55),
                parser.parse("8 окт 21, 10:55")
        );
        assertEquals(
                LocalDateTime.of(2021, Month.SEPTEMBER, 28, 14, 41),
                parser.parse("28 сен 21, 14:41")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidDate() {
        DateTimeParser parser = new SqlRuDateTimeParser();
        parser.parse("сегодня 10:55");
        parser.parse("28 сен 21");
        parser.parse(null);
    }
}