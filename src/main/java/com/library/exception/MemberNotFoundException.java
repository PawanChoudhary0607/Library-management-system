package com.library.exception;

/**
 * Thrown when a lookup for a {@code Member} by id finds no match.
 */
public class MemberNotFoundException extends LibraryException {

    private static final long serialVersionUID = 1L;

    public MemberNotFoundException(String message) {
        super(message);
    }

    public static MemberNotFoundException forId(String memberId) {
        return new MemberNotFoundException("No member found with id: " + memberId);
    }
}
