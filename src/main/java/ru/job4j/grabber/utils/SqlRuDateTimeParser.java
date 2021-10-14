package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.util.Map;

public class SqlRuDateTimeParser implements DateTimeParser {

    private static final String TODAY = "сегодня";
    private static final String YESTERDAY = "вчера";

    private static final Map<String, String> MONTHS = Map.ofEntries(
            Map.entry("янв", "01"),
            Map.entry("фев", "02"),
            Map.entry("мар", "03"),
            Map.entry("апр", "04"),
            Map.entry("май", "05"),
            Map.entry("июн", "06"),
            Map.entry("июл", "07"),
            Map.entry("авг", "08"),
            Map.entry("сен", "09"),
            Map.entry("окт", "10"),
            Map.entry("ноя", "11"),
            Map.entry("дек", "12")
    );

    @Override
    public LocalDateTime parse(String dateString) {
        LocalDateTime dateTime;
        try {
            var dateFormattedString = new StringBuilder();
            var dateStringParts = dateString.split(",");
            var date = dateStringParts[0];
            if (date.equals(TODAY) || date.equals(YESTERDAY)) {
                var todayOrYesterday = date.equals(TODAY)
                        ? LocalDateTime.now()
                        : LocalDateTime.now().minusDays(1);
                dateFormattedString
                        .append(todayOrYesterday.getYear())
                        .append("-")
                        .append(addZeroToNumber(todayOrYesterday.getMonthValue()))
                        .append("-")
                        .append(addZeroToNumber(todayOrYesterday.getDayOfMonth())
                        );
            } else {
                var dateParts = date.split(" ");
                dateFormattedString
                        .append("20")
                        .append(dateParts[2])
                        .append("-")
                        .append(MONTHS.get(dateParts[1]))
                        .append("-")
                        .append(addZeroToNumber(Integer.parseInt(dateParts[0])));
            }
            dateFormattedString
                    .append("T")
                    .append(dateStringParts[1].trim());
            dateTime = LocalDateTime.parse(dateFormattedString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse date string!");
        }
        return dateTime;
    }

    private static String addZeroToNumber(int num) {
        return num < 10 ? "0" + num : String.valueOf(num);
    }
}
