package com.automation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.automation.dto.CreateUserRequest;
import com.automation.dto.UpdateUserRequest;
import com.automation.dto.UserAdminDto;
import com.automation.service.AuthService;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminAuthController {

    private final AuthService authService;

    @Value("${auth.admin-password:happy@123}")
    private String adminPassword;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    private boolean isInvalidAdminPassword(String providedHeader) {
        return providedHeader == null || !providedHeader.equals(adminPassword);
    }

    @PostMapping("/verify-pass")
    public ResponseEntity<?> verifyAdminPassword(@RequestBody Map<String, String> body) {
        String inputPassword = body != null ? body.get("password") : null;
        if (adminPassword.equals(inputPassword)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", adminPassword,
                    "message", "Admin password verified successfully."
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid Admin Password. Access denied."));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listUsers(@RequestHeader(value = "X-Admin-Password", required = false) String adminPassHeader) {
        if (isInvalidAdminPassword(adminPassHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized. Invalid Admin Password."));
        }
        return ResponseEntity.ok(authService.getAllUsersForAdmin());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPassHeader,
            @RequestBody CreateUserRequest request) {
        if (isInvalidAdminPassword(adminPassHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized. Invalid Admin Password."));
        }
        try {
            UserAdminDto user = authService.registerUser(request.getUserId(), request.getUserName(), request.getUserMail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPassHeader,
            @RequestBody UpdateUserRequest request) {
        if (isInvalidAdminPassword(adminPassHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized. Invalid Admin Password."));
        }
        try {
            UserAdminDto user = authService.updateUser(request.getUserId(), request.getUserName(), request.getUserMail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPassHeader,
            @RequestBody Map<String, String> body) {
        if (isInvalidAdminPassword(adminPassHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized. Invalid Admin Password."));
        }
        try {
            String userId = body.get("userId");
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId is required."));
            }
            String tempPassword = authService.resetPasswordByAdmin(userId);
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "tempPassword", tempPassword,
                    "message", "Password reset successfully. Please copy and provide the temporary password to the user."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/block")
    public ResponseEntity<?> blockUser(
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPassHeader,
            @RequestBody Map<String, Object> body) {
        if (isInvalidAdminPassword(adminPassHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized. Invalid Admin Password."));
        }
        try {
            String userId = (String) body.get("userId");
            Boolean blocked = (Boolean) body.get("blocked");
            if (userId == null || blocked == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "userId and blocked status are required."));
            }
            authService.setUserBlockedStatus(userId, blocked);
            return ResponseEntity.ok(Map.of("userId", userId, "blocked", blocked, "message", "User blocked status updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPassHeader,
            @PathVariable("userId") String userId) {
        if (isInvalidAdminPassword(adminPassHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized. Invalid Admin Password."));
        }
        try {
            authService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("userId", userId, "message", "User deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
