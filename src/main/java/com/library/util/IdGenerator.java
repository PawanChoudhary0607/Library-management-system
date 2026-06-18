package com.library.util;

import java.util.UUID;

/**
 * Generates unique entity identifiers using {@link UUID}, optionally
 * with a readable prefix (e.g. {@code "BOOK-<uuid>"}) so ids are easy
 * to tell apart at a glance in logs and console output.
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static String generate(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return generate();
        }
        return prefix + "-" + UUID.randomUUID();
    }
}
