package com.library.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Domain entity representing a single book title held by the library.
 *
 * <p>Tracks how many copies exist in total versus how many are currently
 * available to borrow. Copy-count changes go through {@link #borrowCopy()}
 * and {@link #returnCopy()} rather than a plain setter, so this class can
 * guarantee {@code availableCopies} never goes negative or exceeds
 * {@code totalCopies} during normal application use.
 *
 * <p>Format-level validation (e.g. ISBN structure) is left to
 * {@code IsbnValidator}; this class only enforces structural invariants
 * (non-null fields, non-negative counts).
 */
public class Book {

    private final String id;
    private String isbn;
    private String title;
    private String author;
    private Category category;
    private int totalCopies;
    private int availableCopies;

    @JsonCreator
    public Book(
            @JsonProperty("id") String id,
            @JsonProperty("isbn") String isbn,
            @JsonProperty("title") String title,
            @JsonProperty("author") String author,
            @JsonProperty("category") Category category,
            @JsonProperty("totalCopies") int totalCopies,
            @JsonProperty("availableCopies") int availableCopies) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.isbn = Objects.requireNonNull(isbn, "isbn must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.author = Objects.requireNonNull(author, "author must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");

        if (totalCopies < 0) {
            throw new IllegalArgumentException("totalCopies cannot be negative");
        }
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException("availableCopies must be between 0 and totalCopies");
        }

        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /**
     * Marks one copy as borrowed.
     *
     * @throws IllegalStateException if no copies are currently available
     */
    public void borrowCopy() {
        if (!isAvailable()) {
            throw new IllegalStateException("No available copies of book: " + id);
        }
        availableCopies--;
    }

    /**
     * Marks one copy as returned.
     *
     * @throws IllegalStateException if all copies are already accounted for
     */
    public void returnCopy() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException("All copies already accounted for, book: " + id);
        }
        availableCopies++;
    }

    public String getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = Objects.requireNonNull(isbn, "isbn must not be null");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "title must not be null");
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = Objects.requireNonNull(author, "author must not be null");
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category, "category must not be null");
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("totalCopies cannot be negative");
        }
        if (totalCopies < availableCopies) {
            throw new IllegalArgumentException("totalCopies cannot be less than availableCopies");
        }
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Book{id='" + id + "', isbn='" + isbn + "', title='" + title +
                "', author='" + author + "', category=" + category +
                ", totalCopies=" + totalCopies + ", availableCopies=" + availableCopies + '}';
    }
}
