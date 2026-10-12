package com.automation.aiagent;

import com.automation.aiagent.service.RunbookService;
import com.automation.aiagent.service.TempFileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Realtime Verification Test Suite for AI Runbooks Engine.
 * Tests:
 * 1. Initial directory & starter files (Contents.md, happy_contents.md)
 * 2. Decision: What NOT to save (null action)
 * 3. Decision: What TO save (CREATE action in services/oracle_db.md)
 * 4. Exact Line-level update (UPDATE action on line 2)
 * 5. Append action on user contents profile
 * 6. Directory traversal security guard
 * 7. TempFileService relative path resolution
 */
public class RunbookRealtimeTest {

    private static final String TEST_RUNBOOK_DIR = "C:/Ranjan/AutomationSuite_v2/Runbooks";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Starting AI Runbooks Realtime Verification Test");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        try {
            RunbookService runbookService = new RunbookService(TEST_RUNBOOK_DIR);
            TempFileService tempFileService = new TempFileService("C:/Ranjan/AutomationSuite_v2/ai_temp_files", ".txt,.md,.log");
            tempFileService.setRunbookDirPath(runbookService.getRunbookRoot());

            // -------------------------------------------------------------
            // TEST 1: Starter files generation with Metadata headers
            // -------------------------------------------------------------
            System.out.println("\n[TEST 1] Verifying Starter Files with Metadata Headers...");
            File contentsFile = runbookService.getContentsFile();
            File userFile = runbookService.getUserContentsFile("happy");

            if (contentsFile.exists() && userFile.exists()) {
                String contentsText = Files.readString(contentsFile.toPath(), StandardCharsets.UTF_8);
                String userText = Files.readString(userFile.toPath(), StandardCharsets.UTF_8);

                boolean contentsOk = contentsText.contains("# Runbooks Master Contents Index") &&
                                     contentsText.contains("> Scope: Global");
                boolean userOk = userText.contains("# User Runbook Profile") &&
                                 userText.contains("> User ID: happy");

                if (contentsOk && userOk) {
                    System.out.println(" PASS: Contents.md and happy_contents.md initialized with correct metadata headers.");
                    passed++;
                } else {
                    System.err.println(" FAIL: Metadata headers missing in starter files.");
                    failed++;
                }
            } else {
                System.err.println(" FAIL: Starter files not created.");
                failed++;
            }

            // -------------------------------------------------------------
            // TEST 2: Decision: What NOT to save (null action)
            // -------------------------------------------------------------
            System.out.println("\n[TEST 2] Verifying AI Decision: What NOT to save (null action)...");
            String noActionJson = "{\"feature_id\": null, \"runbook_action\": null, \"final_outcome\": \"Process finished with zero errors.\"}";
            JsonNode noActionNode = MAPPER.readTree(noActionJson);
            if (noActionNode.get("runbook_action").isNull()) {
                Map<String, Object> res = runbookService.applyRunbookAction(null, "happy");
                if (Boolean.FALSE.equals(res.get("success"))) {
                    System.out.println(" PASS: Generic/trivial interaction correctly resulted in no save action.");
                    passed++;
                } else {
                    System.err.println(" FAIL: Null action should not perform file modifications.");
                    failed++;
                }
            }

            // -------------------------------------------------------------
            // TEST 3: Decision: What TO save (CREATE action in services/oracle_db.md)
            // -------------------------------------------------------------
            System.out.println("\n[TEST 3] Verifying AI Decision: What TO save (CREATE action)...");
            String createJson = "{\n" +
                    "  \"runbook_action\": {\n" +
                    "    \"target_file\": \"services/oracle_db.md\",\n" +
                    "    \"action\": \"CREATE\",\n" +
                    "    \"data\": \"# Oracle Database Operational Runbook\\n- Port: 1521\\n- Service Name: ORCL\\n- Health Command: lsnrctl status\\n\",\n" +
                    "    \"index_catalog\": \"GLOBAL\",\n" +
                    "    \"index_description\": \"Oracle DB listener verification and port 1521 configuration\"\n" +
                    "  }\n" +
                    "}";
            JsonNode createNode = MAPPER.readTree(createJson);
            @SuppressWarnings("unchecked")
            Map<String, Object> createMap = MAPPER.convertValue(createNode.get("runbook_action"), Map.class);
            Map<String, Object> createRes = runbookService.applyRunbookAction(createMap, "happy");

            File oracleFile = runbookService.resolveFile("services/oracle_db.md").toFile();
            if (Boolean.TRUE.equals(createRes.get("success")) && oracleFile.exists()) {
                String oracleContent = Files.readString(oracleFile.toPath(), StandardCharsets.UTF_8);
                String contentsCatalog = Files.readString(contentsFile.toPath(), StandardCharsets.UTF_8);

                boolean fileHasData = oracleContent.contains("- Port: 1521");
                boolean indexUpdated = contentsCatalog.contains("- `services/oracle_db.md` : Oracle DB listener verification");

                if (fileHasData && indexUpdated) {
                    System.out.println(" PASS: Created services/oracle_db.md and automatically added index entry to Contents.md.");
                    passed++;
                } else {
                    System.err.println(" FAIL: File created but data or index entry missing.");
                    failed++;
                }
            } else {
                System.err.println(" FAIL: CREATE action failed: " + createRes);
                failed++;
            }

            // -------------------------------------------------------------
            // TEST 4: Exact Line-level update (UPDATE action on line 2)
            // -------------------------------------------------------------
            System.out.println("\n[TEST 4] Verifying Exact Line-Level Update (Port 1521 -> 1522)...");
            String updateJson = "{\n" +
                    "  \"runbook_action\": {\n" +
                    "    \"target_file\": \"services/oracle_db.md\",\n" +
                    "    \"action\": \"UPDATE\",\n" +
                    "    \"start_line\": 2,\n" +
                    "    \"end_line\": 2,\n" +
                    "    \"data\": \"- Port: 1522 (Failover Port)\",\n" +
                    "    \"index_catalog\": \"GLOBAL\",\n" +
                    "    \"index_description\": \"Oracle DB listener failover port 1522\"\n" +
                    "  }\n" +
                    "}";
            JsonNode updateNode = MAPPER.readTree(updateJson);
            @SuppressWarnings("unchecked")
            Map<String, Object> updateMap = MAPPER.convertValue(updateNode.get("runbook_action"), Map.class);
            Map<String, Object> updateRes = runbookService.applyRunbookAction(updateMap, "happy");

            if (Boolean.TRUE.equals(updateRes.get("success"))) {
                List<String> oracleLines = Files.readAllLines(oracleFile.toPath(), StandardCharsets.UTF_8);
                System.out.println("   Resulting file content:");
                for (int i = 0; i < oracleLines.size(); i++) {
                    System.out.println("     Line " + (i + 1) + ": " + oracleLines.get(i));
                }

                boolean line1Intact = oracleLines.get(0).equals("# Oracle Database Operational Runbook");
                boolean line2Updated = oracleLines.get(1).equals("- Port: 1522 (Failover Port)");
                boolean line3Intact = oracleLines.get(2).equals("- Service Name: ORCL");
                boolean line4Intact = oracleLines.get(3).equals("- Health Command: lsnrctl status");

                if (line1Intact && line2Updated && line3Intact && line4Intact) {
                    System.out.println(" PASS: Line 2 successfully updated to port 1522 while preserving all other lines intact.");
                    passed++;
                } else {
                    System.err.println(" FAIL: Surrounding lines were modified or line 2 was not updated properly.");
                    failed++;
                }
            } else {
                System.err.println(" FAIL: UPDATE action failed: " + updateRes);
                failed++;
            }

            // -------------------------------------------------------------
            // TEST 5: APPEND action to user profile
            // -------------------------------------------------------------
            System.out.println("\n[TEST 5] Verifying APPEND action to User Profile...");
            String appendJson = "{\n" +
                    "  \"runbook_action\": {\n" +
                    "    \"target_file\": \"happy_contents.md\",\n" +
                    "    \"action\": \"APPEND\",\n" +
                    "    \"data\": \"- Preferred Host: db-prod-01 (run read-only checks first)\",\n" +
                    "    \"index_catalog\": \"USER\",\n" +
                    "    \"index_description\": null\n" +
                    "  }\n" +
                    "}";
            JsonNode appendNode = MAPPER.readTree(appendJson);
            @SuppressWarnings("unchecked")
            Map<String, Object> appendMap = MAPPER.convertValue(appendNode.get("runbook_action"), Map.class);
            Map<String, Object> appendRes = runbookService.applyRunbookAction(appendMap, "happy");

            if (Boolean.TRUE.equals(appendRes.get("success"))) {
                String userContent = Files.readString(userFile.toPath(), StandardCharsets.UTF_8);
                if (userContent.contains("- Preferred Host: db-prod-01")) {
                    System.out.println(" PASS: Successfully appended operational note to happy_contents.md.");
                    passed++;
                } else {
                    System.err.println(" FAIL: Content not appended to user profile.");
                    failed++;
                }
            } else {
                System.err.println(" FAIL: APPEND action failed: " + appendRes);
                failed++;
            }

            // -------------------------------------------------------------
            // TEST 6: Directory Traversal Security Guard
            // -------------------------------------------------------------
            System.out.println("\n[TEST 6] Verifying Directory Traversal Security Guard...");
            try {
                runbookService.resolveFile("../../Windows/System32/evil.txt");
                System.err.println(" FAIL: Directory traversal was not blocked!");
                failed++;
            } catch (SecurityException se) {
                System.out.println(" PASS: Directory traversal attack blocked with SecurityException: " + se.getMessage());
                passed++;
            }

            // -------------------------------------------------------------
            // TEST 7: TempFileService Relative Runbook Path Resolution
            // -------------------------------------------------------------
            System.out.println("\n[TEST 7] Verifying TempFileService Relative Path Resolution...");
            List<String> resolvedTempPaths = tempFileService.processAndCopyFiles(Collections.singletonList("services/oracle_db.md"));
            if (resolvedTempPaths.size() == 1) {
                File tempCopiedFile = new File(resolvedTempPaths.get(0));
                if (tempCopiedFile.exists()) {
                    System.out.println(" PASS: TempFileService resolved relative path 'services/oracle_db.md' directly to: " + tempCopiedFile.getAbsolutePath());
                    passed++;
                } else {
                    System.err.println(" FAIL: Temp file was not copied properly.");
                    failed++;
                }
            } else {
                System.err.println(" FAIL: TempFileService could not resolve relative runbook path.");
                failed++;
            }

        } catch (Exception e) {
            System.err.println("Unexpected exception during test execution: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("\n=================================================");
        System.out.println(" Realtime Verification Summary:");
        System.out.println(" Passed: " + passed + " / " + (passed + failed));
        System.out.println(" Failed: " + failed);
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
