package com.library.util;

import com.library.config.AppConfig;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Computes the overdue fine for a loan as
 * {@code daysOverdue * finePerDayAmount}. Two constructors are provided:
 * one taking {@link AppConfig} for normal application wiring, and one
 * taking the rate directly so tests don't need to construct a full
 * {@code AppConfig} just to exercise the fine math.
 */
public class FineCalculator {

    private final int finePerDayAmount;

    public FineCalculator(AppConfig appConfig) {
        Objects.requireNonNull(appConfig, "appConfig must not be null");
        this.finePerDayAmount = appConfig.getFinePerDayAmount();
    }

    public FineCalculator(int finePerDayAmount) {
        if (finePerDayAmount < 0) {
            throw new IllegalArgumentException("finePerDayAmount cannot be negative");
        }
        this.finePerDayAmount = finePerDayAmount;
    }

    public int calculateFine(LocalDate dueDate, LocalDate returnDate) {
        Objects.requireNonNull(dueDate, "dueDate must not be null");
        Objects.requireNonNull(returnDate, "returnDate must not be null");

        long overdueDays = DateUtil.daysOverdue(dueDate, returnDate);
        return (int) (overdueDays * finePerDayAmount);
    }
}
