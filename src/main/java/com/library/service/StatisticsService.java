package com.library.service;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.model.TransactionStatus;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import com.library.repository.TransactionRepository;
import com.library.util.DateUtil;
import com.library.util.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Read-only aggregation over books, members, and transactions for
 * dashboards and reporting.
 *
 * <p>Depends only on the three repository interfaces. It has no
 * dependency on {@code ReportGenerator}; turning these numbers into a
 * saved text report is the caller's job (typically the UI layer), which
 * keeps statistics computation separate from output formatting.
 */
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;

    public StatisticsService(BookRepository bookRepository, MemberRepository memberRepository,
                              TransactionRepository transactionRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
    }

    /**
     * High-level snapshot of library activity: catalog/member/transaction
     * counts, currently active loans, currently overdue loans, and total
     * fines collected from completed (returned) loans.
     */
    public DashboardStatistics getDashboardStatistics() {
        List<Book> books = bookRepository.findAll();
        List<Member> members = memberRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();
        LocalDate today = DateUtil.today();

        int activeLoans = (int) transactions.stream()
                .filter(transaction -> transaction.getStatus() != TransactionStatus.RETURNED)
                .count();
        int overdueLoans = (int) transactions.stream()
                .filter(transaction -> transaction.isOverdue(today))
                .count();
        int totalFinesCollected = transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.RETURNED)
                .mapToInt(Transaction::getFineAmount)
                .sum();

        DashboardStatistics stats = new DashboardStatistics(
                books.size(), members.size(), transactions.size(),
                activeLoans, overdueLoans, totalFinesCollected);
        logger.debug("Computed dashboard statistics: {}", stats);
        return stats;
    }

    /**
     * Ranks books by how many times they have ever been borrowed (across
     * all transactions, returned or not), highest first.
     *
     * @param limit maximum number of results to return
     */
    public List<BookBorrowCount> getMostBorrowedBooks(int limit) {
        InputValidator.requirePositive(limit, "limit");

        Map<String, Long> borrowCountsByBookId = transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::getBookId, Collectors.counting()));

        return borrowCountsByBookId.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new BookBorrowCount(
                        entry.getKey(),
                        bookRepository.findById(entry.getKey()).map(Book::getTitle).orElse("Unknown title"),
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * @return all transactions that are not yet returned and past their due date
     */
    public List<Transaction> getOverdueTransactions() {
        LocalDate today = DateUtil.today();
        return transactionRepository.findAll().stream()
                .filter(transaction -> transaction.isOverdue(today))
                .collect(Collectors.toList());
    }

    /**
     * @return all transactions that have not yet been returned
     */
    public List<Transaction> getActiveLoans() {
        return transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getStatus() != TransactionStatus.RETURNED)
                .collect(Collectors.toList());
    }

    /**
     * Snapshot of library-wide counts at a point in time.
     */
    public record DashboardStatistics(
            int totalBooks,
            int totalMembers,
            int totalTransactions,
            int activeLoans,
            int overdueLoans,
            int totalFinesCollected) {
    }

    /**
     * How many times a single book has been borrowed.
     */
    public record BookBorrowCount(String bookId, String title, long borrowCount) {
    }
}
