package com.library;

import com.library.config.AppConfig;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import com.library.repository.TransactionRepository;
import com.library.repository.impl.JsonBookRepository;
import com.library.repository.impl.JsonMemberRepository;
import com.library.repository.impl.JsonTransactionRepository;
import com.library.service.BookService;
import com.library.service.LibraryService;
import com.library.service.MemberService;
import com.library.service.StatisticsService;
import com.library.ui.ConsoleUI;
import com.library.util.JsonFileHandler;
import com.library.util.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Composition root for the application: the only class that instantiates
 * concrete repository implementations. Every dependency below this point
 * is wired through interfaces (manual dependency injection — no
 * framework), and every service depends only on repository interfaces
 * plus {@link AppConfig}, never on another service.
 */
public final class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private App() {
    }

    public static void main(String[] args) {
        AppConfig appConfig = AppConfig.load();
        Path dataDirectory = Paths.get(appConfig.getDataDirectory());

        JsonFileHandler jsonFileHandler = new JsonFileHandler();

        BookRepository bookRepository =
                new JsonBookRepository(dataDirectory.resolve("books.json"), jsonFileHandler);
        MemberRepository memberRepository =
                new JsonMemberRepository(dataDirectory.resolve("members.json"), jsonFileHandler);
        TransactionRepository transactionRepository =
                new JsonTransactionRepository(dataDirectory.resolve("transactions.json"), jsonFileHandler);

        BookService bookService = new BookService(bookRepository);
        MemberService memberService = new MemberService(memberRepository);
        LibraryService libraryService =
                new LibraryService(bookRepository, memberRepository, transactionRepository, appConfig);
        StatisticsService statisticsService =
                new StatisticsService(bookRepository, memberRepository, transactionRepository);

        ReportGenerator reportGenerator = new ReportGenerator();

        logger.info("Library Management System starting up (data directory: {})", dataDirectory);

        ConsoleUI consoleUI = new ConsoleUI(bookService, memberService, libraryService,
                statisticsService, reportGenerator);
        consoleUI.run();

        logger.info("Library Management System shut down");
    }
}
