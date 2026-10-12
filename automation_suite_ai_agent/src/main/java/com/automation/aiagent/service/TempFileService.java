package com.automation.aiagent.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Temp File Service.
 * Manages copying local files to C:\Ranjan\AutomationSuite_v2\ai_temp_files,
 * appends .txt for forbidden file extensions, enforces max 3 file limit, and provides Base64 data.
 */
public class TempFileService {

    private final Path tempDirPath;
    private final Set<String> allowedExtensions;
    private Path runbookDirPath = null;

    public void setRunbookDirPath(Path runbookDirPath) {
        this.runbookDirPath = runbookDirPath;
    }

    public TempFileService(String tempPathStr, String allowedExtsStr) {
        this.tempDirPath = Paths.get(tempPathStr);
        this.allowedExtensions = new HashSet<>();

        if (allowedExtsStr != null && !allowedExtsStr.trim().isEmpty()) {
            for (String ext : allowedExtsStr.split(",")) {
                allowedExtensions.add(ext.trim().toLowerCase());
            }
        }

        try {
            Files.createDirectories(tempDirPath);
            System.out.println("[TempFileService] Initialized temp dir: " + tempDirPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[TempFileService] Could not create temp dir: " + e.getMessage());
        }
    }

    public List<String> processAndCopyFiles(List<String> originalPaths) throws IllegalArgumentException, IOException {
        if (originalPaths == null || originalPaths.isEmpty()) {
            return Collections.emptyList();
        }

        if (originalPaths.size() > 3) {
            throw new IllegalArgumentException("Max 3 files allowed. Provided: " + originalPaths.size());
        }

        List<String> tempFilePaths = new ArrayList<>();

        for (String originalPathStr : originalPaths) {
            if (originalPathStr == null || originalPathStr.trim().isEmpty()) continue;

            Path srcPath = Paths.get(originalPathStr.trim());
            if ((!Files.exists(srcPath) || !Files.isRegularFile(srcPath)) && runbookDirPath != null) {
                Path runbookResolved = runbookDirPath.resolve(originalPathStr.trim()).normalize();
                if (Files.exists(runbookResolved) && Files.isRegularFile(runbookResolved)) {
                    srcPath = runbookResolved;
                }
            }
            if (!Files.exists(srcPath) || !Files.isRegularFile(srcPath)) {
                throw new IOException("File not found on disk: " + originalPathStr);
            }

            String originalFileName = srcPath.getFileName().toString();
            String ext = "";
            int dotIdx = originalFileName.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = originalFileName.substring(dotIdx).toLowerCase();
            }

            String targetFileName = originalFileName;
            if (!ext.isEmpty() && !allowedExtensions.contains(ext)) {
                // Append .txt if extension is forbidden (e.g. xyz.bat -> xyz.bat.txt)
                targetFileName = originalFileName + ".txt";
                System.out.println("[TempFileService] Forbidden extension '" + ext + "'. Disguising file as: " + targetFileName);
            }

            Path destPath = tempDirPath.resolve(targetFileName);
            Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            tempFilePaths.add(destPath.toAbsolutePath().toString());
            System.out.println("[TempFileService] Copied '" + originalFileName + "' to temp path: " + destPath);
        }

        return tempFilePaths;
    }

    public Map<String, Object> readFileData(String filePathStr) throws IOException {
        Path path = Paths.get(filePathStr);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IOException("File not found in temp storage: " + filePathStr);
        }

        byte[] fileBytes = Files.readAllBytes(path);
        String base64 = Base64.getEncoder().encodeToString(fileBytes);
        String fileName = path.getFileName().toString();
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("fileName", fileName);
        map.put("mimeType", mimeType);
        map.put("size", fileBytes.length);
        map.put("base64", base64);
        return map;
    }

    public Path getTempDirPath() {
        return tempDirPath;
    }
}
