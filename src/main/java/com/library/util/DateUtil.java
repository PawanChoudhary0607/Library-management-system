package com.library.util;

import com.library.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Date helpers used for loan due-date calculation and overdue-day
 * counting. Centralized here so the same {@code yyyy-MM-dd} format and
 * the same "how many days late" logic are used everywhere instead of
 * being re-implemented per call site.
 */
public final class DateUtil {

    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private DateUtil() {
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate calculateDueDate(LocalDate issueDate, int loanPeriodDays) {
        Objects.requireNonNull(issueDate, "issueDate must not be null");
        if (loanPeriodDays <= 0) {
            throw new IllegalArgumentException("loanPeriodDays must be positive");
        }
        return issueDate.plusDays(loanPeriodDays);
    }

    /**
     * @return the number of whole days {@code referenceDate} is past
     *         {@code dueDate}, or 0 if not yet overdue
     */
    public static long daysOverdue(LocalDate dueDate, LocalDate referenceDate) {
        Objects.requireNonNull(dueDate, "dueDate must not be null");
        Objects.requireNonNull(referenceDate, "referenceDate must not be null");
        long diff = ChronoUnit.DAYS.between(dueDate, referenceDate);
        return Math.max(diff, 0);
    }

    public static String format(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.format(ISO_DATE_FORMAT);
    }

    public static LocalDate parse(String text) {
        Objects.requireNonNull(text, "text must not be null");
        try {
            return LocalDate.parse(text, ISO_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format, expected yyyy-MM-dd: " + text);
        }
    }
}
