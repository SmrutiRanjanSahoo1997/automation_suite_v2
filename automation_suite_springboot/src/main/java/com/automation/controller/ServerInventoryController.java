package com.automation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.automation.agent.AgentClientManager;
import com.automation.agent.AgentConnectionStatus;
import com.automation.config.ServerInventory;
import com.automation.config.ServerInventory.ServerConfig;
import com.automation.service.TerminalOrchestratorService;

@RestController
@RequestMapping({"/api/v2/servers", "/api/v1/terminal"})
@CrossOrigin(origins = "*")
public class ServerInventoryController {

    private final AgentClientManager agentClientManager;
    private final TerminalOrchestratorService terminalOrchestratorService;

    public ServerInventoryController(AgentClientManager agentClientManager,
                                     TerminalOrchestratorService terminalOrchestratorService) {
        this.agentClientManager = agentClientManager;
        this.terminalOrchestratorService = terminalOrchestratorService;
    }

    @GetMapping("/inventory")
    public List<ServerConfig> getInventory() {
        return ServerInventory.SERVERS;
    }

    @GetMapping("/status")
    public Map<String, AgentConnectionStatus> getStatus() {
        return agentClientManager.getStatusMap();
    }

    @PostMapping("/rename")
    public ResponseEntity<?> renameTerminalPost(@RequestBody Map<String, String> body) {
        String terminalId = body.get("terminalId");
        String newName = body.get("newName");
        if (terminalId == null || newName == null || newName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "terminalId and newName cannot be empty"));
        }
        boolean success = terminalOrchestratorService.renameTerminalSession(terminalId, newName.trim());
        if (success) {
            return ResponseEntity.ok(Map.of("status", "success", "terminalId", terminalId, "name", newName.trim()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Terminal session not found"));
        }
    }

    @PutMapping("/terminals/{terminalId}/rename")
    public ResponseEntity<?> renameTerminal(@PathVariable String terminalId, @RequestBody Map<String, String> body) {
        String newName = body.get("name");
        if (newName == null || newName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name cannot be empty"));
        }
        boolean success = terminalOrchestratorService.renameTerminalSession(terminalId, newName.trim());
        if (success) {
            return ResponseEntity.ok(Map.of("status", "success", "terminalId", terminalId, "name", newName.trim()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Terminal session not found"));
        }
    }
}
