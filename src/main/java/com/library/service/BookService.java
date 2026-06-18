package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateIsbnException;
import com.library.model.Book;
import com.library.model.Category;
import com.library.repository.BookRepository;
import com.library.util.IdGenerator;
import com.library.util.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Business operations for managing the book catalog.
 *
 * <p>Depends only on {@link BookRepository} — no other service, and no
 * file or JSON handling of any kind. Input validation happens here, on
 * every public entry point, regardless of what the caller (console UI
 * today, perhaps something else later) already checked.
 */
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository must not be null");
    }

    /**
     * Registers a new book title in the catalog. {@code availableCopies}
     * starts equal to {@code totalCopies}, since a freshly added title has
     * no copies out on loan yet.
     *
     * @throws DuplicateIsbnException if a book with this ISBN already exists
     */
    public Book addBook(String isbn, String title, String author, Category category, int totalCopies) {
        InputValidator.requireIsbn(isbn);
        InputValidator.requireNonBlank(title, "title");
        InputValidator.requireNonBlank(author, "author");
        Objects.requireNonNull(category, "category must not be null");
        InputValidator.requirePositive(totalCopies, "totalCopies");

        if (bookRepository.existsByIsbn(isbn)) {
            logger.warn("Attempted to add duplicate ISBN: {}", isbn);
            throw new DuplicateIsbnException(isbn);
        }

        Book book = new Book(IdGenerator.generate("BOOK"), isbn, title, author, category,
                totalCopies, totalCopies);
        bookRepository.save(book);
        logger.info("Added book '{}' (id={}, isbn={})", title, book.getId(), isbn);
        return book;
    }

    /**
     * Updates the editable details of an existing book. ISBN is treated as
     * immutable identity and cannot be changed through this method.
     *
     * @throws BookNotFoundException if no book with this id exists
     */
    public Book updateBook(String bookId, String title, String author, Category category, int totalCopies) {
        InputValidator.requireNonBlank(bookId, "bookId");
        InputValidator.requireNonBlank(title, "title");
        InputValidator.requireNonBlank(author, "author");
        Objects.requireNonNull(category, "category must not be null");
        InputValidator.requirePositive(totalCopies, "totalCopies");

        Book book = getBookOrThrow(bookId);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setTotalCopies(totalCopies);

        bookRepository.save(book);
        logger.info("Updated book: {}", bookId);
        return book;
    }

    /**
     * @throws BookNotFoundException if no book with this id exists
     */
    public void deleteBook(String bookId) {
        InputValidator.requireNonBlank(bookId, "bookId");
        if (!bookRepository.deleteById(bookId)) {
            logger.warn("Attempted to delete unknown book id: {}", bookId);
            throw BookNotFoundException.forId(bookId);
        }
        logger.info("Deleted book: {}", bookId);
    }

    /**
     * Case-insensitive search across title, author, and ISBN.
     */
    public List<Book> searchBooks(String keyword) {
        InputValidator.requireNonBlank(keyword, "keyword");
        String lowerKeyword = keyword.toLowerCase();
        return bookRepository.findAll().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(lowerKeyword)
                        || book.getAuthor().toLowerCase().contains(lowerKeyword)
                        || book.getIsbn().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailable();
    }

    private Book getBookOrThrow(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> BookNotFoundException.forId(bookId));
    }
}
