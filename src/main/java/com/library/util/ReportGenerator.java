package com.library.util;

import com.library.exception.DataPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Writes plain-text reports to a timestamped file inside the
 * {@code /reports} directory. Deliberately generic — it accepts a title
 * and a list of already-formatted content lines rather than depending on
 * any specific statistics type, so it has no dependency on the service
 * layer and can be reused for any future report content.
 */
public class ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path reportsDirectory;

    public ReportGenerator() {
        this(Paths.get("reports"));
    }

    public ReportGenerator(Path reportsDirectory) {
        this.reportsDirectory = Objects.requireNonNull(reportsDirectory, "reportsDirectory must not be null");
    }

    /**
     * Writes a timestamped text report and returns the path written to.
     *
     * @param title        heading shown at the top of the report
     * @param contentLines body lines, written in order, one per line
     */
    public Path generateReport(String title, List<String> contentLines) {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(contentLines, "contentLines must not be null");

        try {
            Files.createDirectories(reportsDirectory);
        } catch (IOException e) {
            throw new DataPersistenceException("Unable to create reports directory: " + reportsDirectory, e);
        }

        LocalDateTime now = LocalDateTime.now();
        String fileName = "library-report-" + now.format(FILE_TIMESTAMP) + ".txt";
        Path reportPath = reportsDirectory.resolve(fileName);

        StringBuilder content = new StringBuilder();
        content.append("=".repeat(60)).append(System.lineSeparator());
        content.append(title).append(System.lineSeparator());
        content.append("Generated: ").append(now.format(DISPLAY_TIMESTAMP)).append(System.lineSeparator());
        content.append("=".repeat(60)).append(System.lineSeparator()).append(System.lineSeparator());

        for (String line : contentLines) {
            content.append(line).append(System.lineSeparator());
        }

        try {
            Files.writeString(reportPath, content.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to write report file: " + reportPath, e);
        }

        logger.info("Report written to {}", reportPath);
        return reportPath;
    }
}
