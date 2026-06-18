package com.library.model;

/**
 * Subject category a {@link Book} belongs to.
 *
 * <p>Modeled as an enum rather than a free-text field since the set of
 * categories is small, known in advance, and benefits from compile-time
 * safety over plain strings.
 */
public enum Category {
    FICTION,
    NON_FICTION,
    SCIENCE,
    TECHNOLOGY,
    HISTORY,
    BIOGRAPHY,
    CHILDREN,
    REFERENCE
}
