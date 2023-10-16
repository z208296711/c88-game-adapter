package com.c88.game.adapter.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

    private DateUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final long TICKS_AT_EPOCH = 621355968000000000L;
    private static final long TICKS_PER_MILLISECOND = 10000;

    public static LocalDateTime convertFromTicks(Long ticks) {
        long time = (ticks - TICKS_AT_EPOCH) / TICKS_PER_MILLISECOND;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time),
                ZoneId.of("-8"));
    }

    public static Long toTimestamp(String dateString, String pattern, Integer timeZone) {
        LocalDateTime dateTime;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        dateTime = LocalDateTime.parse(dateString, dateTimeFormatter);
        return dateTime.toInstant(ZoneOffset.ofHours(timeZone)).toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(String dateString, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateString, dateTimeFormatter);
    }

    public static String toDateString(Long timestamp, String pattern) {
        Date date = new Date(timestamp);
        DateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
}
