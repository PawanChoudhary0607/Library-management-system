package com.library.repository.impl;

import com.library.model.Transaction;
import com.library.model.TransactionStatus;
import com.library.repository.TransactionRepository;
import com.library.util.JsonFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JSON-file-backed implementation of {@link TransactionRepository}.
 * Follows the same read-mutate-write pattern as {@code JsonBookRepository};
 * see that class for the rationale.
 */
public class JsonTransactionRepository implements TransactionRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonTransactionRepository.class);

    private final Path dataFile;
    private final JsonFileHandler jsonFileHandler;

    public JsonTransactionRepository(Path dataFile, JsonFileHandler jsonFileHandler) {
        this.dataFile = Objects.requireNonNull(dataFile, "dataFile must not be null");
        this.jsonFileHandler = Objects.requireNonNull(jsonFileHandler, "jsonFileHandler must not be null");
    }

    @Override
    public Transaction save(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        List<Transaction> transactions = loadAll();
        transactions.removeIf(existing -> existing.getId().equals(transaction.getId()));
        transactions.add(transaction);
        jsonFileHandler.writeList(dataFile, transactions);
        logger.info("Saved transaction: {}", transaction.getId());
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return loadAll().stream()
                .filter(transaction -> transaction.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return loadAll();
    }

    @Override
    public List<Transaction> findByMemberId(String memberId) {
        Objects.requireNonNull(memberId, "memberId must not be null");
        return loadAll().stream()
                .filter(transaction -> transaction.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByBookId(String bookId) {
        Objects.requireNonNull(bookId, "bookId must not be null");
        return loadAll().stream()
                .filter(transaction -> transaction.getBookId().equals(bookId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByStatus(TransactionStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return loadAll().stream()
                .filter(transaction -> transaction.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        List<Transaction> transactions = loadAll();
        boolean removed = transactions.removeIf(transaction -> transaction.getId().equals(id));
        if (removed) {
            jsonFileHandler.writeList(dataFile, transactions);
            logger.info("Deleted transaction: {}", id);
        } else {
            logger.warn("Delete requested for unknown transaction id: {}", id);
        }
        return removed;
    }

    private List<Transaction> loadAll() {
        return new ArrayList<>(jsonFileHandler.readList(dataFile, Transaction.class));
    }
}
