package com.library.util;

/**
 * Validates ISBN-10 and ISBN-13 strings, including checksum digits —
 * not just length/format, so a plausible-looking but invalid number is
 * still rejected.
 */
public final class IsbnValidator {

    private IsbnValidator() {
    }

    public static boolean isValid(String isbn) {
        if (isbn == null) {
            return false;
        }
        String normalized = normalize(isbn);
        if (normalized.length() == 10) {
            return isValidIsbn10(normalized);
        }
        if (normalized.length() == 13) {
            return isValidIsbn13(normalized);
        }
        return false;
    }

    private static String normalize(String isbn) {
        return isbn.replaceAll("[\\s-]", "").toUpperCase();
    }

    private static boolean isValidIsbn10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);
            int digit;
            if (i == 9 && c == 'X') {
                digit = 10;
            } else if (Character.isDigit(c)) {
                digit = c - '0';
            } else {
                return false;
            }
            sum += digit * (10 - i);
        }
        return sum % 11 == 0;
    }

    private static boolean isValidIsbn13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            char c = isbn.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int digit = c - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }
}
