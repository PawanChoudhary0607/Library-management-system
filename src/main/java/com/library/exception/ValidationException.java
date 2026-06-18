package com.library.exception;

/**
 * Thrown when user-supplied input fails validation (format, required
 * fields, range checks, etc.) before it reaches a domain entity.
 */
public class ValidationException extends LibraryException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
