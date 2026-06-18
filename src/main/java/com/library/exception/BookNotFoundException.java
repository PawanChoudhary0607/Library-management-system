package com.library.exception;

/**
 * Thrown when a lookup for a {@code Book} by id or ISBN finds no match.
 */
public class BookNotFoundException extends LibraryException {

    private static final long serialVersionUID = 1L;

    public BookNotFoundException(String message) {
        super(message);
    }

    public static BookNotFoundException forId(String bookId) {
        return new BookNotFoundException("No book found with id: " + bookId);
    }

    public static BookNotFoundException forIsbn(String isbn) {
        return new BookNotFoundException("No book found with ISBN: " + isbn);
    }
}
