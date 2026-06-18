package com.library.exception;

/**
 * Thrown when reading from or writing to the underlying JSON data files
 * fails. Wraps the originating {@code IOException} so callers can log
 * or inspect the root cause without the persistence layer leaking
 * checked exceptions into the rest of the application.
 */
public class DataPersistenceException extends LibraryException {

    private static final long serialVersionUID = 1L;

    public DataPersistenceException(String message) {
        super(message);
    }

    public DataPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
