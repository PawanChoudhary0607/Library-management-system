package com.library.config;

import com.library.exception.DataPersistenceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Central configuration for library business rules and the data
 * directory location, loaded from {@code application.properties} on the
 * classpath. Falls back to sensible defaults for any property that is
 * missing, so the application still runs even with no properties file
 * present at all.
 */
public class AppConfig {

    private static final String PROPERTIES_RESOURCE = "application.properties";
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    private static final int DEFAULT_FINE_PER_DAY_AMOUNT = 5;
    private static final String DEFAULT_DATA_DIRECTORY = "src/main/resources/data";

    private final int loanPeriodDays;
    private final int finePerDayAmount;
    private final String dataDirectory;

    public AppConfig(int loanPeriodDays, int finePerDayAmount, String dataDirectory) {
        if (loanPeriodDays <= 0) {
            throw new IllegalArgumentException("loanPeriodDays must be positive");
        }
        if (finePerDayAmount < 0) {
            throw new IllegalArgumentException("finePerDayAmount cannot be negative");
        }
        this.loanPeriodDays = loanPeriodDays;
        this.finePerDayAmount = finePerDayAmount;
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory must not be null");
        if (this.dataDirectory.isBlank()) {
            throw new IllegalArgumentException("dataDirectory must not be blank");
        }
    }

    /**
     * Default configuration: a 14-day loan period, a fine of 5 (currency
     * units) per day overdue, and the standard project data directory.
     */
    public static AppConfig defaultConfig() {
        return new AppConfig(DEFAULT_LOAN_PERIOD_DAYS, DEFAULT_FINE_PER_DAY_AMOUNT, DEFAULT_DATA_DIRECTORY);
    }

    /**
     * Loads configuration from {@code application.properties} on the
     * classpath. Any property not present falls back to its default
     * individually, rather than discarding the whole file.
     *
     * @throws DataPersistenceException if the properties file exists but cannot be read
     */
    public static AppConfig load() {
        Properties properties = new Properties();
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to load " + PROPERTIES_RESOURCE, e);
        }

        int loanPeriodDays = parseIntOrDefault(
                properties.getProperty("library.loan.period.days"), DEFAULT_LOAN_PERIOD_DAYS);
        int finePerDayAmount = parseIntOrDefault(
                properties.getProperty("library.fine.per.day"), DEFAULT_FINE_PER_DAY_AMOUNT);
        String dataDirectory = properties.getProperty("library.data.directory", DEFAULT_DATA_DIRECTORY);

        return new AppConfig(loanPeriodDays, finePerDayAmount, dataDirectory);
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }

    public int getFinePerDayAmount() {
        return finePerDayAmount;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }
}
