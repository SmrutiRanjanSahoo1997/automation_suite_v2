package com.automation.aiagent.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Runbook Service.
 * Manages host operational runbooks under ai.agent.runbook.path.
 * Supports dynamic AI-driven folders, starter file generation,
 * line-level updates, and safe relative path resolution.
 */
public class RunbookService {

    private final Path runbookRoot;
    private final String rawRunbookPath;

    public RunbookService(String runbookRootPath) {
        this.rawRunbookPath = runbookRootPath;
        this.runbookRoot = Paths.get(runbookRootPath).toAbsolutePath().normalize();
        initDirectoryStructure();
    }

    public String getRawRunbookPath() {
        return rawRunbookPath;
    }

    public Path getRunbookRoot() {
        return runbookRoot;
    }

    public synchronized void initDirectoryStructure() {
        try {
            if (!Files.exists(runbookRoot)) {
                Files.createDirectories(runbookRoot);
                System.out.println("[RunbookService] Created Runbooks root directory: " + runbookRoot);
            }

            // Ensure Contents.md starter file exists
            Path contentsPath = runbookRoot.resolve("Contents.md");
            if (!Files.exists(contentsPath)) {
                String starterContents =
                        "# Runbooks Master Contents Index\n" +
                        "> Subtitle: This file contains the directory index of available runbooks, file descriptions, and short global operational notes.\n" +
                        "> Scope: Global\n\n" +
                        "## 1. Runbook Index\n" +
                        "<!-- Entries added dynamically by AI when creating or updating runbooks -->\n\n" +
                        "## 2. Quick Reference Data\n" +
                        "<!-- Short operational facts and snippets added dynamically by AI -->\n";
                Files.writeString(contentsPath, starterContents, StandardCharsets.UTF_8);
                System.out.println("[RunbookService] Initialized starter Contents.md");
            }
        } catch (IOException e) {
            System.err.println("[RunbookService] Error initializing Runbooks directory: " + e.getMessage());
        }
    }

    public File getContentsFile() {
        initDirectoryStructure();
        return runbookRoot.resolve("Contents.md").toFile();
    }

    public synchronized File getUserContentsFile(String userId) {
        initDirectoryStructure();
        String safeUserId = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : "default";
        Path userPath = runbookRoot.resolve(safeUserId + "_contents.md");

        if (!Files.exists(userPath)) {
            String starterUser =
                    "# User Runbook Profile\n" +
                    "> Subtitle: This file contains user-specific operational notes, preferences, and custom commands.\n" +
                    "> User ID: " + safeUserId + "\n\n" +
                    "## 1. User Preferences\n" +
                    "<!-- AI records user-specific execution styles and frequently used hosts -->\n\n" +
                    "## 2. User Saved Notes & Runbooks\n" +
                    "<!-- AI records custom playbooks and shortcuts specific to this user -->\n";
            try {
                Files.writeString(userPath, starterUser, StandardCharsets.UTF_8);
                System.out.println("[RunbookService] Initialized starter user profile: " + userPath.getFileName());
            } catch (IOException e) {
                System.err.println("[RunbookService] Error creating user contents file: " + e.getMessage());
            }
        }
        return userPath.toFile();
    }

    public Path resolveFile(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Runbook file path cannot be empty");
        }
        Path targetPath = runbookRoot.resolve(relativePath.trim()).normalize();
        if (!targetPath.startsWith(runbookRoot)) {
            throw new SecurityException("Directory traversal forbidden: " + relativePath);
        }
        return targetPath;
    }

    /**
     * Executes AI runbook_action: CREATE, APPEND, or UPDATE (line replacement).
     */
    public synchronized Map<String, Object> applyRunbookAction(Map<String, Object> actionData, String userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        if (actionData == null || actionData.isEmpty()) {
            result.put("message", "No runbook_action provided");
            return result;
        }

        String targetFile = (String) actionData.get("target_file");
        String action = (String) actionData.get("action");
        String data = (String) actionData.get("data");
        Number startLineNum = (Number) actionData.get("start_line");
        Number endLineNum = (Number) actionData.get("end_line");
        String indexCatalog = (String) actionData.get("index_catalog");
        String indexDesc = (String) actionData.get("index_description");

        if (targetFile == null || targetFile.trim().isEmpty()) {
            result.put("message", "target_file is required");
            return result;
        }

        if (action == null || action.trim().isEmpty()) {
            action = "CREATE";
        }
        action = action.trim().toUpperCase();

        try {
            Path filePath = resolveFile(targetFile);

            if ("CREATE".equals(action)) {
                if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
                    Files.createDirectories(filePath.getParent());
                }
                Files.writeString(filePath, data != null ? data : "", StandardCharsets.UTF_8);
                System.out.println("[RunbookService] Created runbook file: " + filePath);
                updateIndex(targetFile, indexDesc, indexCatalog, userId);
                result.put("success", true);
                result.put("message", "File created: " + targetFile);
            } else if ("APPEND".equals(action)) {
                if (!Files.exists(filePath)) {
                    if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
                        Files.createDirectories(filePath.getParent());
                    }
                    Files.writeString(filePath, (data != null ? data : "") + "\n", StandardCharsets.UTF_8);
                } else {
                    Files.writeString(filePath, "\n" + (data != null ? data : "") + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                }
                System.out.println("[RunbookService] Appended to runbook file: " + filePath);
                updateIndex(targetFile, indexDesc, indexCatalog, userId);
                result.put("success", true);
                result.put("message", "Content appended: " + targetFile);
            } else if ("UPDATE".equals(action)) {
                if (!Files.exists(filePath)) {
                    result.put("message", "Cannot update non-existent file: " + targetFile);
                    return result;
                }

                List<String> lines = new ArrayList<>(Files.readAllLines(filePath, StandardCharsets.UTF_8));
                int totalLines = lines.size();

                int startLine = startLineNum != null ? startLineNum.intValue() : 1;
                int endLine = endLineNum != null ? endLineNum.intValue() : totalLines;

                if (startLine < 1) startLine = 1;
                if (endLine > totalLines) endLine = totalLines;
                if (startLine > endLine) {
                    result.put("message", "Invalid line range: " + startLine + " to " + endLine + " (file has " + totalLines + " lines)");
                    return result;
                }

                int startIdx = startLine - 1; // 0-based
                int endIdx = endLine - 1;

                List<String> newLines = new ArrayList<>();
                // Keep preceding lines
                for (int i = 0; i < startIdx; i++) {
                    newLines.add(lines.get(i));
                }
                // Insert new data
                if (data != null && !data.isEmpty()) {
                    String[] replacementRows = data.split("\r?\n");
                    newLines.addAll(Arrays.asList(replacementRows));
                }
                // Keep following lines
                for (int i = endIdx + 1; i < totalLines; i++) {
                    newLines.add(lines.get(i));
                }

                Files.write(filePath, newLines, StandardCharsets.UTF_8);
                System.out.println("[RunbookService] Updated lines " + startLine + "-" + endLine + " in: " + filePath);
                updateIndex(targetFile, indexDesc, indexCatalog, userId);
                result.put("success", true);
                result.put("message", "Updated lines " + startLine + "-" + endLine + " in " + targetFile);
            } else {
                result.put("message", "Unsupported action: " + action);
                return result;
            }

            result.put("targetFile", targetFile);
            result.put("action", action);
        } catch (Exception e) {
            System.err.println("[RunbookService] Error applying runbook action: " + e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    private void updateIndex(String targetFile, String indexDesc, String indexCatalog, String userId) {
        if (indexDesc == null || indexDesc.trim().isEmpty()) return;
        if (targetFile.equalsIgnoreCase("Contents.md") || targetFile.toLowerCase().endsWith("_contents.md")) return;

        try {
            boolean isUser = "USER".equalsIgnoreCase(indexCatalog);
            Path catalogPath = isUser ? getUserContentsFile(userId).toPath() : getContentsFile().toPath();

            List<String> lines = new ArrayList<>(Files.readAllLines(catalogPath, StandardCharsets.UTF_8));
            String entryPrefix = "- `" + targetFile + "` : ";
            String newEntry = entryPrefix + indexDesc.trim();

            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains("`" + targetFile + "`")) {
                    lines.set(i, newEntry);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                // Find insertion point under section header
                int insertIdx = -1;
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).startsWith("## 1.") || lines.get(i).startsWith("## 2. User Saved Notes")) {
                        insertIdx = i + 1;
                        break;
                    }
                }
                if (insertIdx != -1 && insertIdx < lines.size()) {
                    lines.add(insertIdx + 1, newEntry);
                } else {
                    lines.add(newEntry);
                }
            }

            Files.write(catalogPath, lines, StandardCharsets.UTF_8);
            System.out.println("[RunbookService] Updated catalog index in " + catalogPath.getFileName() + " for " + targetFile);
        } catch (Exception e) {
            System.err.println("[RunbookService] Could not update catalog index: " + e.getMessage());
        }
    }
}
