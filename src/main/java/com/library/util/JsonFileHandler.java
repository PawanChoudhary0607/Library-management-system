package com.library.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.exception.DataPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

/**
 * Generic Jackson-based JSON list read/write, shared by every JSON
 * repository implementation so file I/O and ObjectMapper configuration
 * exist in exactly one place.
 */
public class JsonFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(JsonFileHandler.class);

    private final ObjectMapper objectMapper;

    public JsonFileHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Reads a JSON array file into a list of the given element type.
     * Returns an empty list if the file does not exist yet, so callers
     * don't need special handling for a fresh clone with no data file.
     */
    public <T> List<T> readList(Path path, Class<T> elementType) {
        if (!Files.exists(path)) {
            logger.warn("Data file not found, returning empty list: {}", path);
            return Collections.emptyList();
        }
        try {
            JavaType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            List<T> result = objectMapper.readValue(path.toFile(), listType);
            logger.debug("Read {} record(s) from {}", result.size(), path);
            return result;
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to read JSON file: " + path, e);
        }
    }

    /**
     * Writes a list to a JSON array file atomically: data is written to a
     * temporary file in the same directory, then moved into place, so a
     * crash mid-write cannot leave the target file corrupted or truncated.
     */
    public <T> void writeList(Path path, List<T> data) {
        try {
            Path parentDir = path.toAbsolutePath().getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }

            Path tempFile = Files.createTempFile(parentDir, "tmp-", ".json");
            objectMapper.writeValue(tempFile.toFile(), data);
            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            logger.debug("Wrote {} record(s) to {}", data.size(), path);
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to write JSON file: " + path, e);
        }
    }
}
