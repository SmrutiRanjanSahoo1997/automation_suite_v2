package com.automation.aiagent;

import com.automation.aiagent.scheduler.FileCleanupScheduler;
import com.automation.aiagent.server.AiAgentServer;
import com.automation.aiagent.service.ChromeMonitorService;
import com.automation.aiagent.service.TempFileService;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Main Entry Point for Plain Java AI Agent Server.
 * Runs on Port 9020 without Spring Boot.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Starting Automation Suite AI Agent Server...");
        System.out.println("=================================================");

        Properties props = loadConfigProperties();

        int port = Integer.parseInt(props.getProperty("ai.agent.port", "9020"));
        String tempPath = props.getProperty("ai.agent.temp.path", "C:/Ranjan/AutomationSuite_v2/ai_temp_files");
        String allowedExts = props.getProperty("ai.agent.allowed.extensions",
                ".txt,.log,.pdf,.doc,.docx,.xlsx,.xls,.csv,.ppt,.pptx,.xml,.html,.htm,.json,.md,.png,.jpg,.jpeg,.webp,.gif,.bmp,.svg,.py,.java");
        String runbookPath = props.getProperty("ai.agent.runbook.path", "C:/Ranjan/AutomationSuite_v2/Runbooks");

        // 1. Initialize Runbook Service & Temp File Service
        com.automation.aiagent.service.RunbookService runbookService = new com.automation.aiagent.service.RunbookService(runbookPath);
        TempFileService tempFileService = new TempFileService(tempPath, allowedExts);
        tempFileService.setRunbookDirPath(runbookService.getRunbookRoot());

        // 2. Initialize File Cleanup Scheduler
        FileCleanupScheduler fileCleanupScheduler = new FileCleanupScheduler(tempFileService);
        fileCleanupScheduler.start();

        // 3. Initialize Chrome Monitor Service
        ChromeMonitorService chromeMonitorService = new ChromeMonitorService();
        chromeMonitorService.start();

        // 4. Initialize Dual WebSocket Server on Port 9020
        AiAgentServer agentServer = new AiAgentServer(port, tempFileService, chromeMonitorService, runbookService);
        agentServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Main] Shutting down AI Agent Server...");
            agentServer.stopServer();
            chromeMonitorService.stop();
            fileCleanupScheduler.stop();
            System.out.println("[Main] Shutdown complete.");
        }));
    }

    private static Properties loadConfigProperties() {
        Properties props = new Properties();
        File configFile = new File("config.properties");
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                System.out.println("[Main] Loaded config.properties");
            } catch (Exception e) {
                System.err.println("[Main] Error reading config.properties: " + e.getMessage());
            }
        } else {
            System.out.println("[Main] config.properties not found. Using default settings.");
        }
        return props;
    }
}
