package com.library.service;

import com.library.config.AppConfig;
import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.exception.LibraryException;
import com.library.exception.MemberNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.model.TransactionStatus;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import com.library.repository.TransactionRepository;
import com.library.util.DateUtil;
import com.library.util.FineCalculator;
import com.library.util.IdGenerator;
import com.library.util.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Core library operations: issuing and returning books, fine calculation,
 * and tracking what each member currently has borrowed.
 *
 * <p>Depends only on {@link BookRepository}, {@link MemberRepository},
 * {@link TransactionRepository}, and {@link AppConfig} — not on
 * {@code BookService} or {@code MemberService} — so every cross-entity
 * rule in this class (a member must be active, a book must have an
 * available copy, etc.) is enforced exactly once, here, rather than
 * relying on checks already performed by another service.
 */
public class LibraryService {

    private static final Logger logger = LoggerFactory.getLogger(LibraryService.class);

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final FineCalculator fineCalculator;
    private final int loanPeriodDays;

    public LibraryService(BookRepository bookRepository, MemberRepository memberRepository,
                           TransactionRepository transactionRepository, AppConfig appConfig) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
        Objects.requireNonNull(appConfig, "appConfig must not be null");
        this.fineCalculator = new FineCalculator(appConfig);
        this.loanPeriodDays = appConfig.getLoanPeriodDays();
    }

    /**
     * Issues a book to a member: validates both exist and are eligible,
     * decrements the book's available copies, and records a new
     * {@code ISSUED} transaction due back after the configured loan period.
     *
     * @throws MemberNotFoundException if no member with this id exists
     * @throws ValidationException if the member is not active
     * @throws BookNotFoundException if no book with this id exists
     * @throws BookNotAvailableException if the book has no available copies
     */
    public Transaction issueBook(String memberId, String bookId) {
        InputValidator.requireNonBlank(memberId, "memberId");
        InputValidator.requireNonBlank(bookId, "bookId");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> MemberNotFoundException.forId(memberId));
        if (!member.isActive()) {
            throw new ValidationException("Member is not active and cannot borrow books: " + memberId);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> BookNotFoundException.forId(bookId));
        if (!book.isAvailable()) {
            throw new BookNotAvailableException(bookId);
        }

        book.borrowCopy();
        bookRepository.save(book);

        LocalDate issueDate = DateUtil.today();
        LocalDate dueDate = DateUtil.calculateDueDate(issueDate, loanPeriodDays);
        Transaction transaction = Transaction.issueNew(
                IdGenerator.generate("TXN"), bookId, memberId, issueDate, dueDate);
        transactionRepository.save(transaction);

        logger.info("Issued book {} to member {} (transaction {}), due {}",
                bookId, memberId, transaction.getId(), dueDate);
        return transaction;
    }

    /**
     * Returns a borrowed book: calculates any overdue fine, marks the
     * transaction returned, and restores the book's available copy count.
     *
     * @throws LibraryException if no transaction with this id exists, or it was already returned
     */
    public Transaction returnBook(String transactionId) {
        InputValidator.requireNonBlank(transactionId, "transactionId");

        Transaction transaction = getTransactionOrThrow(transactionId);
        if (transaction.getStatus() == TransactionStatus.RETURNED) {
            throw new LibraryException("Transaction already returned: " + transactionId);
        }

        LocalDate returnDate = DateUtil.today();
        int fineAmount = fineCalculator.calculateFine(transaction.getDueDate(), returnDate);
        transaction.markReturned(returnDate, fineAmount);
        transactionRepository.save(transaction);

        bookRepository.findById(transaction.getBookId()).ifPresentOrElse(
                book -> {
                    book.returnCopy();
                    bookRepository.save(book);
                },
                () -> logger.warn("Returned book {} no longer exists in catalog; copy count not restored",
                        transaction.getBookId())
        );

        logger.info("Returned transaction {} (fine={})", transactionId, fineAmount);
        return transaction;
    }

    /**
     * Calculates the fine for a transaction: the actual fine charged if it
     * has already been returned, or the fine that would apply if returned
     * today otherwise.
     *
     * @throws LibraryException if no transaction with this id exists
     */
    public int calculateFine(String transactionId) {
        Transaction transaction = getTransactionOrThrow(transactionId);
        LocalDate referenceDate = transaction.getReturnDate() != null
                ? transaction.getReturnDate()
                : DateUtil.today();
        return fineCalculator.calculateFine(transaction.getDueDate(), referenceDate);
    }

    /**
     * @return the books a given member currently has on loan (not yet returned)
     */
    public List<Transaction> getBorrowedBooks(String memberId) {
        InputValidator.requireNonBlank(memberId, "memberId");
        return transactionRepository.findByMemberId(memberId).stream()
                .filter(transaction -> transaction.getStatus() != TransactionStatus.RETURNED)
                .collect(Collectors.toList());
    }

    private Transaction getTransactionOrThrow(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new LibraryException("No transaction found with id: " + transactionId));
    }
}
