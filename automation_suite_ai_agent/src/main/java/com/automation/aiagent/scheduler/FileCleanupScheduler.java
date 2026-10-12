package com.automation.aiagent.scheduler;

import com.automation.aiagent.service.TempFileService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Hourly Temp File Cleanup Scheduler.
 * Scans C:\Ranjan\AutomationSuite_v2\ai_temp_files every 1 hour and deletes files older than 1 hour.
 */
public class FileCleanupScheduler {

    private static final long ONE_HOUR_MS = 3600000L;
    private final TempFileService tempFileService;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public FileCleanupScheduler(TempFileService tempFileService) {
        this.tempFileService = tempFileService;
    }

    public void start() {
        // Run cleanup every 1 hour (initial delay 10 minutes)
        executor.scheduleAtFixedRate(this::cleanOldFiles, 10, 60, TimeUnit.MINUTES);
        System.out.println("[FileCleanupScheduler] Initialized hourly file cleanup scheduler.");
    }

    public void cleanOldFiles() {
        try {
            Path tempDir = tempFileService.getTempDirPath();
            if (!Files.exists(tempDir)) return;

            File[] files = tempDir.toFile().listFiles();
            if (files == null || files.length == 0) return;

            long now = System.currentTimeMillis();
            int deletedCount = 0;

            for (File file : files) {
                if (file.isFile()) {
                    long ageMs = now - file.lastModified();
                    if (ageMs > ONE_HOUR_MS) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            deletedCount++;
                            System.out.println("[FileCleanupScheduler] Deleted expired temp file (age > 1hr): " + file.getName());
                        }
                    }
                }
            }

            if (deletedCount > 0) {
                System.out.println("[FileCleanupScheduler] Cleaned up " + deletedCount + " temp file(s).");
            }
        } catch (Exception e) {
            System.err.println("[FileCleanupScheduler] Error during temp file cleanup: " + e.getMessage());
        }
    }

    public void stop() {
        executor.shutdown();
    }
}
