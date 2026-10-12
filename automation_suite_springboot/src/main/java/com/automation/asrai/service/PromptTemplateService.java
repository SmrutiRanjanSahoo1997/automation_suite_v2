package com.automation.asrai.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.automation.config.ServerInventory;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PromptTemplateService {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${ai.prompt.path:C:/Ranjan/AutomationSuite_v2/PromptTesting/final_prompts}")
    private String promptDirPath;

    public String buildHostSelectionPrompt(String userQuery) {
        String template = loadTemplate("TargetHostSelectionPrompt.txt");
        String inventoryJson = buildServerInventoryJson();
        return template.replace("{USER_QUERY}", escapePromptValue(userQuery))
                       .replace("{SERVER_INVENTORY}", inventoryJson);
    }

    public String buildDiagnosticsRemediationPrompt(String userQuery, String hostname) {
        String template = loadTemplate("SystemDiagnosticsRemediationPrompt.txt");
        if (template.contains("{HOSTNAME}")) {
            template = template.replace("{HOSTNAME}", hostname);
        }
        if (template.contains("{USER_QUERY}")) {
            template = template.replace("{USER_QUERY}", escapePromptValue(userQuery));
        }
        return template;
    }

    public String buildDatabaseQueryPrompt(String userQuery) {
        String template = loadTemplate("DatabaseQueryGenerationPrompt.txt");
        return template.replace("{USER_QUERY}", escapePromptValue(userQuery));
    }

    public String buildDatabaseResultSynthesisPrompt(String userQuery, String environment, String sql, String resultsJson) {
        String template = loadTemplate("DatabaseResultSynthesisPrompt.txt");
        return template.replace("{USER_QUERY}", escapePromptValue(userQuery))
                       .replace("{ENVIRONMENT}", environment != null ? environment.toUpperCase() : "PROD")
                       .replace("{EXECUTED_SQL}", sql != null ? sql : "")
                       .replace("{QUERY_RESULTS}", resultsJson != null ? resultsJson : "[]");
    }

    private String loadTemplate(String filename) {
        try {
            Path path = Path.of(promptDirPath, filename);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
            // Fallback to alternate folder if missing
            Path altPath = Path.of("C:\\Ranjan\\AutomationSuite_v2\\PromptTesting\\Finalised Prompts", filename);
            if (Files.exists(altPath)) {
                return Files.readString(altPath);
            }
        } catch (Exception e) {
            log.error("Failed to read prompt file {}: {}", filename, e.getMessage());
        }
        log.warn("Using fallback internal template for {}", filename);
        return getFallbackTemplate(filename);
    }

    private String buildServerInventoryJson() {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(ServerInventory.SERVERS);
        } catch (Exception e) {
            log.error("Error serializing ServerInventory: {}", e.getMessage());
            return "[]";
        }
    }

    private String escapePromptValue(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
    }

    private String getFallbackTemplate(String filename) {
        if ("TargetHostSelectionPrompt.txt".equalsIgnoreCase(filename)) {
            return """
                You are an AI Server Infrastructure & Target Resolution Agent. You MUST respond ONLY with a single valid raw JSON object matching the schema below.
                User Input Query: "{USER_QUERY}"
                Server Inventory:
                {SERVER_INVENTORY}
                Output JSON Schema:
                {
                  "pre_response": "<short status message>",
                  "target_hosts": ["H1187"],
                  "review_required": false,
                  "review_title": null,
                  "review_description": null,
                  "final_outcome": "<explanation>"
                }
                Return ONLY the JSON object.
                """;
        } else if ("DatabaseQueryGenerationPrompt.txt".equalsIgnoreCase(filename)) {
            return """
                You are an MSSQL Database Query & Automation Agent. Output ONLY a single raw JSON object matching the schema.
                User Query: "{USER_QUERY}"
                Output JSON Schema:
                {
                  "environment": "prod",
                  "pre_response": "<status>",
                  "sql": "<T-SQL query>",
                  "review_needed": false,
                  "review_title": "<title>",
                  "review_description": "<description>",
                  "accumulated_context_data": "<context>",
                  "final_outcome": null
                }
                """;
        } else if ("DatabaseResultSynthesisPrompt.txt".equalsIgnoreCase(filename)) {
            return """
                You are an MSSQL Database Query Synthesis Agent. Output ONLY a single raw JSON object matching the schema.
                Original User Query: "{USER_QUERY}"
                Executed SQL Query ({ENVIRONMENT}): {EXECUTED_SQL}
                Query Execution Results: {QUERY_RESULTS}
                Output JSON Schema:
                {
                  "pre_response": "<status>",
                  "final_outcome": "<markdown synthesis>",
                  "accumulated_context_data": "<context>"
                }
                """;
        } else {
            return """
                You are an AI System Automation Agent. You MUST respond ONLY with a single valid raw JSON object matching the schema below.
                User Query: "{USER_QUERY}"
                Target Hostname: "{HOSTNAME}"
                Output JSON Schema:
                {
                  "host": "{HOSTNAME}",
                  "pre_response": "<short status message>",
                  "Command": "<command or null>",
                  "platform": "PS",
                  "review_needed": false,
                  "review_title": null,
                  "review_description": null,
                  "CriticalityLevele": 0,
                  "file_needed": null,
                  "final_outcome": "<summary>"
                }
                Return ONLY the JSON object.
                """;
        }
    }
}
