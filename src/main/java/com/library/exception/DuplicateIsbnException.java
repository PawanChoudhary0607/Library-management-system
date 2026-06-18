package com.library.exception;

/**
 * Thrown when adding a book whose ISBN already exists in the catalog.
 */
public class DuplicateIsbnException extends LibraryException {

    private static final long serialVersionUID = 1L;

    public DuplicateIsbnException(String isbn) {
        super("A book with ISBN " + isbn + " already exists");
    }
}
