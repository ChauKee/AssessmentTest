package com.vanguard.assessment.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public final class ValidationUtils {

    private static final BigDecimal DEFAULT_DECIMAL_MAX = new BigDecimal(Integer.MAX_VALUE);

   public static boolean isValidDateFormat(String input, DateTimeFormatter datetimeFormatter) {
        try {
            LocalDate.parse(input, datetimeFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }



    public static boolean isValidDecimalRange(String input, BigDecimal min) {
        return isValidDecimalRange(input, min, DEFAULT_DECIMAL_MAX);
    }

    public static boolean isValidDecimalRange(String input, BigDecimal min, BigDecimal max) {
        try {
            BigDecimal bd = new BigDecimal(input);
            return bd.compareTo(min) >= 0 && bd.compareTo(max) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidIntegerRange(String input, int min, int max) {
        try {
            int i = Integer.parseInt(input);
            return i >= min && i <= max;
        } catch (NumberFormatException e) {
            return false;
        }

    }


}


