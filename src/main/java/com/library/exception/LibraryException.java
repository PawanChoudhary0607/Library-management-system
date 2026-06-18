package com.library.exception;

/**
 * Base type for all domain exceptions in the library system. Extends
 * {@link RuntimeException} deliberately: callers across the service and
 * UI layers should not be forced into try/catch boilerplate for every
 * operation, and a single unchecked hierarchy lets the UI layer catch
 * {@code LibraryException} once at its boundary and present a clean
 * message instead of a stack trace.
 */
public class LibraryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
