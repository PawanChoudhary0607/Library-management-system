package com.library.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain entity representing a single book-loan transaction: one book
 * issued to one member, with its due date, return date, and any fine.
 *
 * <p>The full eight-argument constructor mirrors the JSON shape exactly,
 * so Jackson can reconstruct a transaction in any persisted state
 * (issued, overdue, or already returned) directly from disk. For
 * creating a brand-new loan in application code, use
 * {@link #issueNew(String, String, String, LocalDate, LocalDate)} rather
 * than calling the constructor directly. Once a transaction exists, the
 * only way to record a return is {@link #markReturned(LocalDate, int)} —
 * {@link #setStatus(TransactionStatus)} explicitly refuses to transition
 * to {@code RETURNED} so the return event's three related fields
 * (status, returnDate, fineAmount) can never be updated independently
 * and left inconsistent.
 */
public class Transaction {

    private final String id;
    private final String bookId;
    private final String memberId;
    private final LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private int fineAmount;
    private TransactionStatus status;

    @JsonCreator
    public Transaction(
            @JsonProperty("id") String id,
            @JsonProperty("bookId") String bookId,
            @JsonProperty("memberId") String memberId,
            @JsonProperty("issueDate") LocalDate issueDate,
            @JsonProperty("dueDate") LocalDate dueDate,
            @JsonProperty("returnDate") LocalDate returnDate,
            @JsonProperty("fineAmount") int fineAmount,
            @JsonProperty("status") TransactionStatus status) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.bookId = Objects.requireNonNull(bookId, "bookId must not be null");
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.issueDate = Objects.requireNonNull(issueDate, "issueDate must not be null");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");

        if (fineAmount < 0) {
            throw new IllegalArgumentException("fineAmount cannot be negative");
        }
        if (status == TransactionStatus.RETURNED && returnDate == null) {
            throw new IllegalArgumentException("returnDate is required when status is RETURNED");
        }
        if (status != TransactionStatus.RETURNED && returnDate != null) {
            throw new IllegalArgumentException("returnDate must be null unless status is RETURNED");
        }

        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
    }

    /**
     * Creates a brand-new transaction for a book being issued right now —
     * the standard entry point for application code, as opposed to the
     * full constructor, which exists primarily for Jackson deserialization.
     */
    public static Transaction issueNew(String id, String bookId, String memberId,
                                        LocalDate issueDate, LocalDate dueDate) {
        return new Transaction(id, bookId, memberId, issueDate, dueDate,
                null, 0, TransactionStatus.ISSUED);
    }

    /**
     * @param referenceDate the date to compare against (normally "today")
     * @return true if this transaction is not yet returned and past its due date
     */
    public boolean isOverdue(LocalDate referenceDate) {
        return status != TransactionStatus.RETURNED && referenceDate.isAfter(dueDate);
    }

    /**
     * Atomically records the return of this loan: sets the return date,
     * the fine, and the status together so they can never drift apart.
     *
     * @throws IllegalStateException if the transaction was already returned
     * @throws IllegalArgumentException if fineAmount is negative
     */
    public void markReturned(LocalDate returnDate, int fineAmount) {
        if (this.status == TransactionStatus.RETURNED) {
            throw new IllegalStateException("Transaction already returned: " + id);
        }
        Objects.requireNonNull(returnDate, "returnDate must not be null");
        if (returnDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("returnDate cannot be before issueDate");
        }
        if (fineAmount < 0) {
            throw new IllegalArgumentException("fineAmount cannot be negative");
        }
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = TransactionStatus.RETURNED;
    }

    public String getId() {
        return id;
    }

    public String getBookId() {
        return bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate must not be null");
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public int getFineAmount() {
        return fineAmount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    /**
     * Updates the status to anything except {@code RETURNED} — for example
     * transitioning ISSUED to OVERDUE. Returning a book must go through
     * {@link #markReturned(LocalDate, int)} instead.
     */
    public void setStatus(TransactionStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        if (status == TransactionStatus.RETURNED) {
            throw new IllegalStateException(
                    "Use markReturned(...) to transition a transaction to RETURNED");
        }
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{id='" + id + "', bookId='" + bookId + "', memberId='" + memberId +
                "', issueDate=" + issueDate + ", dueDate=" + dueDate +
                ", returnDate=" + returnDate + ", fineAmount=" + fineAmount +
                ", status=" + status + '}';
    }
}
