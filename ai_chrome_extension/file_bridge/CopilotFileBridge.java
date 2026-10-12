package com.automation.bridge;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Standalone Lightweight Java File Bridge (Port 9020).
 * Single responsibility: Serves local Windows file raw bytes to the Chrome Extension
 * via HTTP GET /file?path=...
 */
public class CopilotFileBridge {

    private static final int PORT = 9020;
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".txt", ".log", ".pdf", ".doc", ".docx", ".xlsx", ".xls", ".csv",
            ".ppt", ".pptx", ".xml", ".html", ".htm", ".json", ".md",
            ".png", ".jpg", ".jpeg", ".webp", ".gif", ".bmp", ".svg",
            ".py", ".java"
    ));

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
            
            server.createContext("/health", new HealthHandler());
            server.createContext("/file", new FileHandler());
            server.createContext("/config", new ConfigHandler());

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();

            System.out.println("=================================================");
            System.out.println(" Copilot Java File Bridge Server Running!");
            System.out.println(" Listening on: http://127.0.0.1:" + PORT);
            System.out.println(" Endpoints: GET /health, GET /file?path=..., GET /config");
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Failed to start CopilotFileBridge server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String response = "{"
                    + "\"status\":\"connected\","
                    + "\"agentRunning\":true,"
                    + "\"copilotReachable\":true,"
                    + "\"bridge\":\"CopilotFileBridge Java (Port 9020)\""
                    + "}";

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String response = "{"
                    + "\"auto_minimize\":false,"
                    + "\"automation_mode\":\"auto\","
                    + "\"allowed_extensions\":[\".txt\",\".log\",\".pdf\",\".doc\",\".docx\",\".xlsx\",\".xls\",\".csv\",\".ppt\",\".pptx\",\".xml\",\".html\",\".htm\",\".json\",\".md\",\".png\",\".jpg\",\".jpeg\",\".webp\",\".gif\",\".bmp\",\".svg\",\".py\",\".java\"]"
                    + "}";

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String filePathStr = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2 && "path".equalsIgnoreCase(pair[0])) {
                        filePathStr = URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name());
                        break;
                    }
                }
            }

            if (filePathStr == null || filePathStr.trim().isEmpty()) {
                sendError(exchange, 400, "Query parameter 'path' is required.");
                return;
            }

            Path path = Paths.get(filePathStr.trim());
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                sendError(exchange, 404, "File not found on disk: " + filePathStr);
                return;
            }

            String fileName = path.getFileName().toString();
            String ext = "";
            int dotIdx = fileName.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = fileName.substring(dotIdx).toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                sendError(exchange, 400, "File extension not allowed: " + ext);
                return;
            }

            try {
                byte[] fileBytes = Files.readAllBytes(path);
                String base64 = Base64.getEncoder().encodeToString(fileBytes);
                String mimeType = Files.probeContentType(path);
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }

                String responseJson = "{"
                        + "\"success\":true,"
                        + "\"fileName\":\"" + escapeJson(fileName) + "\","
                        + "\"mimeType\":\"" + escapeJson(mimeType) + "\","
                        + "\"size\":" + fileBytes.length + ","
                        + "\"base64\":\"" + base64 + "\""
                        + "}";

                byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                System.out.println("[SERVED FILE] " + fileName + " (" + fileBytes.length + " bytes)");

            } catch (Exception e) {
                sendError(exchange, 500, "Failed to read file: " + e.getMessage());
            }
        }

        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            String json = "{\"success\":false,\"error\":\"" + escapeJson(message) + "\"}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String escapeJson(String raw) {
            if (raw == null) return "";
            return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }
    }
}
