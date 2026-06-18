package com.library.repository;

import com.library.model.Transaction;
import com.library.model.TransactionStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for {@link Transaction} entities. See
 * {@link BookRepository} for the rationale behind the interface/
 * implementation split.
 */
public interface TransactionRepository {

    /**
     * Inserts a new transaction or overwrites the existing one with the
     * same id — used both when issuing a book and when persisting the
     * state change from {@code Transaction.markReturned(...)}.
     */
    Transaction save(Transaction transaction);

    Optional<Transaction> findById(String id);

    List<Transaction> findAll();

    List<Transaction> findByMemberId(String memberId);

    List<Transaction> findByBookId(String bookId);

    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * @return true if a transaction with the given id was found and removed
     */
    boolean deleteById(String id);
}
