package com.automation.agent.simulator;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Java Simulation Test Suite for Automation Suite AI Agent (Port 9020).
 * Verifies all 8 steps of the Copilot workflow:
 *  - GET /health, GET /config
 *  - Step 4 Payload Validation: prompt mandatory, max 3 files, extension whitelist
 *  - Invalid conversation handling ("The conversation no longer exists.")
 *  - New conversation turn (conversationId = null)
 *  - Existing conversation turn (conversationId = "...")
 *  - Returns response format { conversationId: "...", response: "..." }
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AiAgentSimulationTest {

    private static final Logger log = LoggerFactory.getLogger(AiAgentSimulationTest.class);
    private static final String BASE_URL = "http://127.0.0.1:9020";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .proxy(java.net.ProxySelector.of(null))
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static String createdConversationId = null;

    @BeforeAll
    static void checkServerStatus() {
        try {
            java.net.URL url = java.net.URI.create(BASE_URL + "/health").toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            int statusCode = conn.getResponseCode();
            assertEquals(200, statusCode, "AI Agent service must be running on port 9020");
            log.info("[SETUP] Connected to Automation Suite AI Agent on {}", BASE_URL);
        } catch (Exception e) {
            fail("Automation Suite AI Agent is not running on port 9020. Please start app.py on port 9020 first. Error: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Step 1: GET /health - Verify Health Check Response")
    void testHealthEndpoint() throws Exception {
        HttpResponseInfo response = sendGetJson("/health");
        assertEquals(200, response.statusCode);

        JsonNode json = MAPPER.readTree(response.body);
        log.info("[TEST /health] Payload: {}", json.toPrettyString());

        assertTrue(json.has("status"));
        assertTrue(json.has("agentRunning"));
        assertTrue(json.has("copilotReachable"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /config - Verify Agent Config & Allowed Extensions")
    void testConfigEndpoint() throws Exception {
        HttpResponseInfo response = sendGetJson("/config");
        assertEquals(200, response.statusCode);

        JsonNode json = MAPPER.readTree(response.body);
        log.info("[TEST /config] Config payload: {}", json.toPrettyString());

        assertTrue(json.has("allowed_extensions"));
        assertTrue(json.path("allowed_extensions").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("Step 4 Validation: Max 3 File Paths Limit Exception")
    void testValidationMaxFilesLimit() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.putNull("conversationId");
        reqNode.put("prompt", "Test max files limit");
        ArrayNode filesNode = reqNode.putArray("files_path");
        filesNode.add("file1.txt");
        filesNode.add("file2.log");
        filesNode.add("file3.pdf");
        filesNode.add("file4.docx"); // 4th file -> exceeds max 3!

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Max Files] Status: {}, Body: {}", response.statusCode, response.body);
        assertEquals(400, response.statusCode);
        assertTrue(response.body.contains("Max 3 file paths allowed"));
    }

    @Test
    @Order(4)
    @DisplayName("Step 4 Validation: Forbidden File Extension Exception")
    void testValidationForbiddenExtension() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.putNull("conversationId");
        reqNode.put("prompt", "Test forbidden extension");
        ArrayNode filesNode = reqNode.putArray("files_path");
        filesNode.add("malicious_script.exe");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Forbidden Ext] Status: {}, Body: {}", response.statusCode, response.body);
        assertEquals(400, response.statusCode);
        assertTrue(response.body.contains("File extension not allowed"));
    }

    @Test
    @Order(41)
    @DisplayName("Step 4 Validation: Allowed File Extensions (.py, .java, .doc, .pdf)")
    void testValidationAllowedExtensions() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.putNull("conversationId");
        reqNode.put("prompt", "Test allowed file extensions");
        ArrayNode filesNode = reqNode.putArray("files_path");
        filesNode.add("script.py");
        filesNode.add("Main.java");
        filesNode.add("document.doc");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Allowed Ext] Status: {}, Body: {}", response.statusCode, response.body);
        // Should not fail validation with 400 for file extension
        assertFalse(response.body.contains("File extension not allowed"));
    }

    @Test
    @Order(5)
    @DisplayName("Step 4 Validation: Invalid Deleted Conversation Popup Handling")
    void testInvalidConversationHandling() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.put("conversationId", "Q33spmv6P5vvtt8CW4Aa"); // Invalid/deleted conversation ID
        reqNode.put("prompt", "Hello on deleted chat");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Invalid Conv] Status: {}, Body: {}", response.statusCode, response.body);
        
        // Should return 404 or failed status with "The conversation no longer exists."
        assertTrue(response.statusCode == 404 || response.body.contains("The conversation no longer exists."));
    }

    @Test
    @Order(6)
    @DisplayName("Step 4, 5, 6, 7, 8: New Conversation Chat Turn (conversationId = null)")
    void testNewConversationTurn() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.putNull("conversationId");
        reqNode.put("prompt", "Hello Copilot, reply with exactly the word: BANANA");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST New Chat Turn] Status: {}", response.statusCode);
        assertEquals(200, response.statusCode);

        JsonNode json = MAPPER.readTree(response.body);
        log.info("[TEST New Chat Turn] Response Payload:\n{}", json.toPrettyString());

        assertTrue(json.has("response"), "Response must contain 'response' text");
        assertTrue(json.has("conversationId"), "Response must contain 'conversationId'");

        createdConversationId = json.path("conversationId").asText();
        log.info("[TEST New Chat Turn] Created conversationId: {}", createdConversationId);
        assertNotNull(createdConversationId);
        assertFalse(createdConversationId.isBlank());
    }

    @Test
    @Order(7)
    @DisplayName("Step 4, 5, 7, 8: Existing Conversation Follow-Up Turn")
    void testExistingConversationTurn() throws Exception {
        String convId = createdConversationId != null ? createdConversationId : "sim_conv_12345";

        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.put("conversationId", convId);
        reqNode.put("prompt", "What is 10 + 10?");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Existing Chat Turn] Status: {}", response.statusCode);
        assertEquals(200, response.statusCode);

        JsonNode json = MAPPER.readTree(response.body);
        log.info("[TEST Existing Chat Turn] Response Payload:\n{}", json.toPrettyString());

        assertTrue(json.has("response"));
        assertEquals(convId, json.path("conversationId").asText());
    }

    private HttpResponseInfo sendGetJson(String path) throws Exception {
        java.net.URL url = java.net.URI.create(BASE_URL + path).toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int statusCode = conn.getResponseCode();
        java.io.InputStream is = statusCode < 400 ? conn.getInputStream() : conn.getErrorStream();
        String body = "";
        if (is != null) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                body = sb.toString();
            }
        }
        return new HttpResponseInfo(statusCode, body);
    }

    private HttpResponseInfo sendPostJson(String path, String jsonPayload) throws Exception {
        java.net.URL url = java.net.URI.create(BASE_URL + path).toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(120000);
        conn.setDoOutput(true);

        try (java.io.OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int statusCode = conn.getResponseCode();
        java.io.InputStream is = statusCode < 400 ? conn.getInputStream() : conn.getErrorStream();
        String body = "";
        if (is != null) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                body = sb.toString();
            }
        }
        return new HttpResponseInfo(statusCode, body);
    }

    private static class HttpResponseInfo {
        final int statusCode;
        final String body;
        HttpResponseInfo(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
