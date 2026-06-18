package com.library.repository;

import com.library.model.Book;
import com.library.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for {@link Book} entities.
 *
 * <p>Kept as an interface so the storage mechanism — currently JSON files
 * via {@code JsonBookRepository} — can be swapped for a database-backed
 * implementation later without any change to the Service or UI layers.
 * Business rules (e.g. rejecting a duplicate ISBN) are deliberately not
 * enforced here; this layer only persists what it is given. {@code
 * existsByIsbn} is exposed so the Service layer can check that rule
 * itself before calling {@link #save(Book)}.
 */
public interface BookRepository {

    /**
     * Inserts a new book or overwrites the existing one with the same id.
     */
    Book save(Book book);

    Optional<Book> findById(String id);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findAll();

    List<Book> findByCategory(Category category);

    /**
     * Case-insensitive substring search over book titles.
     */
    List<Book> findByTitleContaining(String keyword);

    /**
     * @return all books with at least one available copy
     */
    List<Book> findAvailable();

    boolean existsByIsbn(String isbn);

    /**
     * @return true if a book with the given id was found and removed
     */
    boolean deleteById(String id);
}
