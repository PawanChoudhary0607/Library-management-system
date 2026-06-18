package com.library.model;

/**
 * State of a book-loan {@link Transaction}.
 */
public enum TransactionStatus {
    ISSUED,
    RETURNED,
    OVERDUE
}
