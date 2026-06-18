package com.library.exception;

/**
 * Thrown when attempting to issue a book that currently has zero
 * available copies.
 */
public class BookNotAvailableException extends LibraryException {

    private static final long serialVersionUID = 1L;

    public BookNotAvailableException(String bookId) {
        super("Book is not available for issue: " + bookId);
    }
}
