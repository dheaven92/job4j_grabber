package ru.job4j.grabber.util;

import java.time.LocalDateTime;

public interface DateTimeParser {

    LocalDateTime parse(String dateString);
}
