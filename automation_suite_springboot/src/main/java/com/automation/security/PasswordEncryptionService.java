package com.automation.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncryptionService {

    @Value("${auth.salt-key:AutomationSuiteV2SecretSaltKey2026!}")
    private String saltKey;

    public String encryptPassword(String rawPassword) {
        if (rawPassword == null) return "";
        String salted = rawPassword + saltKey;
        return Base64.getEncoder().encodeToString(salted.getBytes(StandardCharsets.UTF_8));
    }

    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        String computedHash = encryptPassword(rawPassword);
        return computedHash.equals(storedHash);
    }
}
