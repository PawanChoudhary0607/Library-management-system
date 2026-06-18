package com.library.util;

import com.library.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * General-purpose input validation. Every method throws
 * {@link ValidationException} on failure rather than returning a
 * boolean, so callers (typically Service-layer methods) can validate a
 * whole batch of inputs as a sequence of statements without needing
 * their own if/throw boilerplate after each check.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,13}$");

    private InputValidator() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
    }

    public static void requireEmail(String email) {
        requireNonBlank(email, "email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void requirePhone(String phone) {
        requireNonBlank(phone, "phone");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Invalid phone number format: " + phone);
        }
    }

    public static void requireIsbn(String isbn) {
        requireNonBlank(isbn, "isbn");
        if (!IsbnValidator.isValid(isbn)) {
            throw new ValidationException("Invalid ISBN: " + isbn);
        }
    }

    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }

    public static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative");
        }
    }
}
