package com.pmo.userservice.infrastructure.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtil {

    /**
     * Formats a LocalDate object to a String according to the provided format string.
     *
     * @param date the LocalDate object to format.
     * @return the formatted date as a String.
     */
    public static String ConvertToEmailFormat(LocalDate date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.US_DATE_PATTERN);
            return date.format(formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
