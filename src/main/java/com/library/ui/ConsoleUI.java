package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Category;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.service.BookService;
import com.library.service.LibraryService;
import com.library.service.MemberService;
import com.library.service.StatisticsService;
import com.library.service.StatisticsService.BookBorrowCount;
import com.library.service.StatisticsService.DashboardStatistics;
import com.library.util.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Single console-based UI for the library system. Deliberately one class
 * — internally organized into one private method per menu/feature — as
 * a deliberate simplification for v1. If this file grows past roughly
 * 400-500 lines, that is the signal to split it into per-domain menu
 * classes; for now, no method here contains business logic, only
 * input/output and delegation to the service layer.
 */
public class ConsoleUI {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);

    private final BookService bookService;
    private final MemberService memberService;
    private final LibraryService libraryService;
    private final StatisticsService statisticsService;
    private final ReportGenerator reportGenerator;
    private final Scanner scanner;

    public ConsoleUI(BookService bookService, MemberService memberService,
                      LibraryService libraryService, StatisticsService statisticsService,
                      ReportGenerator reportGenerator) {
        this.bookService = Objects.requireNonNull(bookService, "bookService must not be null");
        this.memberService = Objects.requireNonNull(memberService, "memberService must not be null");
        this.libraryService = Objects.requireNonNull(libraryService, "libraryService must not be null");
        this.statisticsService = Objects.requireNonNull(statisticsService, "statisticsService must not be null");
        this.reportGenerator = Objects.requireNonNull(reportGenerator, "reportGenerator must not be null");
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> handleBookMenu();
                    case 2 -> handleMemberMenu();
                    case 3 -> handleIssueBook();
                    case 4 -> handleReturnBook();
                    case 5 -> handleStatisticsDashboard();
                    case 6 -> handleExportReport();
                    case 7 -> running = false;
                    default -> System.out.println("Invalid option. Please choose 1-7.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
                logger.warn("Handled library exception: {}", e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred. Please try again.");
                logger.error("Unexpected error in ConsoleUI", e);
            }
        }
        System.out.println("Goodbye!");
    }

    // ---------------------------------------------------------------
    // Main menu
    // ---------------------------------------------------------------

    private void printBanner() {
        System.out.println("==============================================");
        System.out.println(" Library Management System");
        System.out.println("==============================================");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("---------------- MAIN MENU ----------------");
        System.out.println("1. Book Management");
        System.out.println("2. Member Management");
        System.out.println("3. Issue Book");
        System.out.println("4. Return Book");
        System.out.println("5. Statistics Dashboard");
        System.out.println("6. Export Report");
        System.out.println("7. Exit");
        System.out.println("---------------------------------------------");
    }

    // ---------------------------------------------------------------
    // Book management
    // ---------------------------------------------------------------

    private void handleBookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("------------- BOOK MANAGEMENT -------------");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. Search Book");
            System.out.println("5. View All Books");
            System.out.println("6. View Available Books");
            System.out.println("7. Back to Main Menu");
            System.out.println("--------------------------------------------");
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> addBook();
                    case 2 -> updateBook();
                    case 3 -> deleteBook();
                    case 4 -> searchBooks();
                    case 5 -> printBooks(bookService.getAllBooks());
                    case 6 -> printBooks(bookService.getAvailableBooks());
                    case 7 -> back = true;
                    default -> System.out.println("Invalid option. Please choose 1-7.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
                logger.warn("Handled library exception: {}", e.getMessage());
            }
        }
    }

    private void addBook() {
        String isbn = readNonBlank("ISBN: ");
        String title = readNonBlank("Title: ");
        String author = readNonBlank("Author: ");
        Category category = readCategory();
        int totalCopies = readInt("Total copies: ");

        Book book = bookService.addBook(isbn, title, author, category, totalCopies);
        System.out.println("Book added successfully with id: " + book.getId());
    }

    private void updateBook() {
        String bookId = readNonBlank("Book id to update: ");
        String title = readNonBlank("New title: ");
        String author = readNonBlank("New author: ");
        Category category = readCategory();
        int totalCopies = readInt("New total copies: ");

        bookService.updateBook(bookId, title, author, category, totalCopies);
        System.out.println("Book updated successfully.");
    }

    private void deleteBook() {
        String bookId = readNonBlank("Book id to delete: ");
        bookService.deleteBook(bookId);
        System.out.println("Book deleted successfully.");
    }

    private void searchBooks() {
        String keyword = readNonBlank("Search keyword (title/author/ISBN): ");
        printBooks(bookService.searchBooks(keyword));
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.printf("%-10s %-15s %-25s %-18s %-12s %5s %5s%n",
                "ID", "ISBN", "Title", "Author", "Category", "Total", "Avail");
        for (Book book : books) {
            System.out.printf("%-10s %-15s %-25s %-18s %-12s %5d %5d%n",
                    book.getId(), book.getIsbn(), truncate(book.getTitle(), 25),
                    truncate(book.getAuthor(), 18), book.getCategory(),
                    book.getTotalCopies(), book.getAvailableCopies());
        }
    }

    // ---------------------------------------------------------------
    // Member management
    // ---------------------------------------------------------------

    private void handleMemberMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("------------ MEMBER MANAGEMENT ------------");
            System.out.println("1. Register Member");
            System.out.println("2. Search Member");
            System.out.println("3. View All Members");
            System.out.println("4. Back to Main Menu");
            System.out.println("--------------------------------------------");
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> registerMember();
                    case 2 -> searchMembers();
                    case 3 -> printMembers(memberService.getAllMembers());
                    case 4 -> back = true;
                    default -> System.out.println("Invalid option. Please choose 1-4.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
                logger.warn("Handled library exception: {}", e.getMessage());
            }
        }
    }

    private void registerMember() {
        String name = readNonBlank("Name: ");
        String email = readNonBlank("Email: ");
        String phone = readNonBlank("Phone: ");

        Member member = memberService.registerMember(name, email, phone);
        System.out.println("Member registered successfully with id: " + member.getId());
    }

    private void searchMembers() {
        String keyword = readNonBlank("Search keyword (name/email): ");
        printMembers(memberService.searchMembers(keyword));
    }

    private void printMembers(List<Member> members) {
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.printf("%-10s %-20s %-25s %-15s %-12s %-10s%n",
                "ID", "Name", "Email", "Phone", "Since", "Status");
        for (Member member : members) {
            System.out.printf("%-10s %-20s %-25s %-15s %-12s %-10s%n",
                    member.getId(), truncate(member.getName(), 20), truncate(member.getEmail(), 25),
                    member.getPhone(), member.getMembershipDate(), member.getStatus());
        }
    }

    // ---------------------------------------------------------------
    // Issue / return
    // ---------------------------------------------------------------

    private void handleIssueBook() {
        String memberId = readNonBlank("Member id: ");
        String bookId = readNonBlank("Book id: ");
        Transaction transaction = libraryService.issueBook(memberId, bookId);
        System.out.println("Book issued. Transaction id: " + transaction.getId()
                + ", due date: " + transaction.getDueDate());
    }

    private void handleReturnBook() {
        String transactionId = readNonBlank("Transaction id: ");
        Transaction transaction = libraryService.returnBook(transactionId);
        if (transaction.getFineAmount() > 0) {
            System.out.println("Book returned. Fine due: " + transaction.getFineAmount());
        } else {
            System.out.println("Book returned. No fine due.");
        }
    }

    // ---------------------------------------------------------------
    // Statistics / reporting
    // ---------------------------------------------------------------

    private void handleStatisticsDashboard() {
        DashboardStatistics stats = statisticsService.getDashboardStatistics();
        System.out.println();
        System.out.println("------------- STATISTICS DASHBOARD -------------");
        System.out.println("Total books:           " + stats.totalBooks());
        System.out.println("Total members:         " + stats.totalMembers());
        System.out.println("Total transactions:    " + stats.totalTransactions());
        System.out.println("Active loans:          " + stats.activeLoans());
        System.out.println("Overdue loans:         " + stats.overdueLoans());
        System.out.println("Total fines collected: " + stats.totalFinesCollected());
        System.out.println();
        System.out.println("Top borrowed books:");
        List<BookBorrowCount> topBooks = statisticsService.getMostBorrowedBooks(5);
        if (topBooks.isEmpty()) {
            System.out.println("  (no transactions yet)");
        } else {
            for (BookBorrowCount entry : topBooks) {
                System.out.printf("  %-25s borrowed %d time(s)%n", truncate(entry.title(), 25), entry.borrowCount());
            }
        }
        System.out.println("--------------------------------------------------");
    }

    private void handleExportReport() {
        DashboardStatistics stats = statisticsService.getDashboardStatistics();
        List<BookBorrowCount> topBooks = statisticsService.getMostBorrowedBooks(5);

        List<String> lines = new ArrayList<>();
        lines.add("Total books: " + stats.totalBooks());
        lines.add("Total members: " + stats.totalMembers());
        lines.add("Total transactions: " + stats.totalTransactions());
        lines.add("Active loans: " + stats.activeLoans());
        lines.add("Overdue loans: " + stats.overdueLoans());
        lines.add("Total fines collected: " + stats.totalFinesCollected());
        lines.add("");
        lines.add("Top borrowed books:");
        if (topBooks.isEmpty()) {
            lines.add("  (no transactions yet)");
        } else {
            for (BookBorrowCount entry : topBooks) {
                lines.add("  " + entry.title() + " - borrowed " + entry.borrowCount() + " time(s)");
            }
        }

        Path reportPath = reportGenerator.generateReport("Library Statistics Report", lines);
        System.out.println("Report exported to: " + reportPath.toAbsolutePath());
    }

    // ---------------------------------------------------------------
    // Input helpers
    // ---------------------------------------------------------------

    private String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("This field cannot be blank. Please try again.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private Category readCategory() {
        while (true) {
            System.out.println("Categories: " + Arrays.toString(Category.values()));
            System.out.print("Category: ");
            String input = scanner.nextLine().trim().toUpperCase().replace(' ', '_');
            try {
                return Category.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Unrecognized category. Please choose one from the list above.");
            }
        }
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 1) + "\u2026";
    }
}
