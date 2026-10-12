package com.automation.mssql.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DatabaseSavedQueryService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSavedQueryService.class);

    @Value("${AI_RUNBOOKS_ROOT_PATH:C:/ASR_Runbooks/Database_Queries}")
    private String runbooksRootPath;

    private Path rootDir;

    @PostConstruct
    public void init() {
        try {
            this.rootDir = Paths.get(runbooksRootPath);
            if (!Files.exists(rootDir)) {
                Files.createDirectories(rootDir);
                log.info("Created MSSQL Runbooks root directory: {}", rootDir.toAbsolutePath());
            } else {
                log.info("MSSQL Runbooks root directory initialized: {}", rootDir.toAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Could not create MSSQL Runbooks root directory {}: {}", runbooksRootPath, e.getMessage());
        }
    }

    public List<Map<String, Object>> listSavedQueries() {
        if (rootDir == null || !Files.exists(rootDir)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        try (Stream<Path> stream = Files.list(rootDir)) {
            stream.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase().endsWith(".sql"))
                  .sorted((a, b) -> a.getFileName().compareTo(b.getFileName()))
                  .forEach(p -> {
                      try {
                          BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                          String fn = p.getFileName().toString();
                          String displayName = fn.endsWith(".sql") ? fn.substring(0, fn.length() - 4) : fn;
                          Map<String, Object> item = new HashMap<>();
                          item.put("fileName", fn);
                          item.put("name", displayName);
                          item.put("lastModified", attrs.lastModifiedTime().toMillis());
                          item.put("size", attrs.size());
                          results.add(item);
                      } catch (Exception ex) {
                          log.warn("Error reading attributes for {}: {}", p, ex.getMessage());
                      }
                  });
        } catch (IOException e) {
            log.error("Failed to list saved queries in {}: {}", rootDir, e.getMessage());
        }

        return results;
    }

    public String getSavedQueryContent(String fileName) throws IOException {
        Path filePath = resolveSafePath(fileName);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Query file not found: " + fileName);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    public void saveQuery(String fileName, String sql) throws IOException {
        String cleanName = sanitizeFileName(fileName);
        if (!cleanName.toLowerCase().endsWith(".sql")) {
            cleanName += ".sql";
        }

        Path filePath = resolveSafePath(cleanName);

        // Pure Java UNC invariant: delete before write to avoid locking/caching on network shares
        Files.deleteIfExists(filePath);
        Files.writeString(filePath, sql != null ? sql : "", StandardCharsets.UTF_8);
        log.info("Saved query successfully to {}", filePath.toAbsolutePath());
    }

    public boolean deleteSavedQuery(String fileName) throws IOException {
        Path filePath = resolveSafePath(fileName);
        boolean deleted = Files.deleteIfExists(filePath);
        log.info("Deleted query {}: result={}", fileName, deleted);
        return deleted;
    }

    private Path resolveSafePath(String fileName) {
        String safeName = Paths.get(fileName).getFileName().toString();
        return rootDir.resolve(safeName);
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "unnamed_query";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}
