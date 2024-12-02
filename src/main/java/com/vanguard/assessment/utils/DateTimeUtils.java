package com.vanguard.assessment.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    public static void main(String[] args) {

        LocalDateTime now = LocalDateTime.now();
        String s = now.format(DATE_TIME_FORMATTER);
        System.out.println(s);
        LocalDateTime.parse(s, DATE_TIME_FORMATTER);

    }

}
