package com.library.repository.impl;

import com.library.model.Book;
import com.library.model.Category;
import com.library.repository.BookRepository;
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
 * JSON-file-backed implementation of {@link BookRepository}.
 *
 * <p>Each call reads the current contents of the data file via
 * {@link JsonFileHandler}, operates on an in-memory copy, and — for
 * mutating operations — writes the full list back atomically. Any
 * {@code IOException} encountered during that process surfaces as an
 * unchecked {@code DataPersistenceException} from {@code JsonFileHandler}
 * itself, so this class does not need its own try/catch blocks around
 * persistence calls.
 */
public class JsonBookRepository implements BookRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonBookRepository.class);

    private final Path dataFile;
    private final JsonFileHandler jsonFileHandler;

    public JsonBookRepository(Path dataFile, JsonFileHandler jsonFileHandler) {
        this.dataFile = Objects.requireNonNull(dataFile, "dataFile must not be null");
        this.jsonFileHandler = Objects.requireNonNull(jsonFileHandler, "jsonFileHandler must not be null");
    }

    @Override
    public Book save(Book book) {
        Objects.requireNonNull(book, "book must not be null");
        List<Book> books = loadAll();
        books.removeIf(existing -> existing.getId().equals(book.getId()));
        books.add(book);
        jsonFileHandler.writeList(dataFile, books);
        logger.info("Saved book: {}", book.getId());
        return book;
    }

    @Override
    public Optional<Book> findById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return loadAll().stream()
                .filter(book -> book.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        Objects.requireNonNull(isbn, "isbn must not be null");
        return loadAll().stream()
                .filter(book -> book.getIsbn().equalsIgnoreCase(isbn))
                .findFirst();
    }

    @Override
    public List<Book> findAll() {
        return loadAll();
    }

    @Override
    public List<Book> findByCategory(Category category) {
        Objects.requireNonNull(category, "category must not be null");
        return loadAll().stream()
                .filter(book -> book.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByTitleContaining(String keyword) {
        Objects.requireNonNull(keyword, "keyword must not be null");
        String lowerKeyword = keyword.toLowerCase();
        return loadAll().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findAvailable() {
        return loadAll().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return findByIsbn(isbn).isPresent();
    }

    @Override
    public boolean deleteById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        List<Book> books = loadAll();
        boolean removed = books.removeIf(book -> book.getId().equals(id));
        if (removed) {
            jsonFileHandler.writeList(dataFile, books);
            logger.info("Deleted book: {}", id);
        } else {
            logger.warn("Delete requested for unknown book id: {}", id);
        }
        return removed;
    }

    /**
     * Loads the current data file into a fresh, mutable {@code ArrayList}.
     * {@code JsonFileHandler} may return an immutable empty list when the
     * file doesn't exist yet, so callers that need to mutate the result
     * (e.g. {@code removeIf}) always go through this method rather than
     * using {@code readList} directly.
     */
    private List<Book> loadAll() {
        return new ArrayList<>(jsonFileHandler.readList(dataFile, Book.class));
    }
}
