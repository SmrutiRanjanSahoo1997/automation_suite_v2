package com.automation.mssql.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.automation.mssql.service.DatabaseSavedQueryService;

@RestController
@RequestMapping("/api/v2/database/saved-queries")
@CrossOrigin(origins = "*")
public class DatabaseSavedQueryController {

    private final DatabaseSavedQueryService savedQueryService;

    public DatabaseSavedQueryController(DatabaseSavedQueryService savedQueryService) {
        this.savedQueryService = savedQueryService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listQueries() {
        return ResponseEntity.ok(savedQueryService.listSavedQueries());
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<?> getQueryContent(@PathVariable String fileName) {
        try {
            String content = savedQueryService.getSavedQueryContent(fileName);
            return ResponseEntity.ok(Map.of("fileName", fileName, "content", content));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> saveQuery(@RequestBody Map<String, String> body) {
        String fileName = body.get("fileName");
        String content = body.get("content");
        if (fileName == null || fileName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "fileName cannot be blank"));
        }

        try {
            savedQueryService.saveQuery(fileName, content);
            return ResponseEntity.ok(Map.of("success", true, "fileName", fileName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteQuery(@PathVariable String fileName) {
        try {
            boolean deleted = savedQueryService.deleteSavedQuery(fileName);
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "fileName", fileName));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "File not found or already deleted"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
