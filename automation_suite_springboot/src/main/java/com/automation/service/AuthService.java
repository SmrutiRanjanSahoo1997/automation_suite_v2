package com.automation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.automation.dto.AuthResponse;
import com.automation.dto.UserAdminDto;
import com.automation.entity.UserEntity;
import com.automation.repository.UserRepository;
import com.automation.security.JwtTokenService;
import com.automation.security.PasswordEncryptionService;
import com.automation.security.PasswordGenerator;

import jakarta.annotation.PostConstruct;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncryptionService passwordEncryptionService;
    private final JwtTokenService jwtTokenService;

    // Fast in-memory user cache for < 20 users
    private final Map<String, UserCacheDto> userCache = new ConcurrentHashMap<>();

    public static class UserCacheDto {
        public String userId;
        public String userName;
        public String userMail;
        public String passwordHash;
        public String role;
        public boolean isPasswordChangeRequired;
        public boolean isBlocked;
        public String tempPassword; // Plaintext temp password when isPasswordChangeRequired == true

        public UserCacheDto(UserEntity entity, String tempPassword) {
            this.userId = entity.getUserId();
            this.userName = entity.getUserName();
            this.userMail = entity.getUserMail();
            this.passwordHash = entity.getPasswordHash();
            this.role = entity.getRole();
            this.isPasswordChangeRequired = entity.isPasswordChangeRequired();
            this.isBlocked = entity.isBlocked();
            this.tempPassword = tempPassword;
        }
    }

    public AuthService(UserRepository userRepository,
                       PasswordEncryptionService passwordEncryptionService,
                       JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncryptionService = passwordEncryptionService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostConstruct
    public void initUserCache() {
        log.info("Initializing in-memory UserCache from SQLite database...");
        List<UserEntity> all = userRepository.findAll();
        for (UserEntity entity : all) {
            userCache.put(entity.getUserId(), new UserCacheDto(entity, entity.isPasswordChangeRequired() ? "Admin@1234" : null));
        }
        log.info("UserCache initialized with {} user record(s).", userCache.size());
    }

    public UserCacheDto getCachedUser(String userId) {
        if (userId == null) return null;
        return userCache.get(userId);
    }

    public AuthResponse login(String userId, String rawPassword) {
        log.info("Processing login request for userId: {}", userId);

        UserCacheDto cached = userCache.get(userId);
        if (cached == null) {
            // Fallback check DB
            UserEntity entity = userRepository.findById(userId).orElse(null);
            if (entity == null) {
                throw new RuntimeException("Invalid User ID or Password.");
            }
            cached = new UserCacheDto(entity, null);
            userCache.put(userId, cached);
        }

        if (cached.isBlocked) {
            log.warn("Login blocked: userId {} is currently blocked", userId);
            throw new RuntimeException("Account is blocked. Please contact administrator.");
        }

        if (!passwordEncryptionService.matches(rawPassword, cached.passwordHash)) {
            log.warn("Login failed: invalid password for userId {}", userId);
            throw new RuntimeException("Invalid User ID or Password.");
        }

        String token = jwtTokenService.generateToken(cached.userId, cached.role);
        long expireTimestamp = jwtTokenService.getExpirationTimestamp();

        log.info("Login successful for userId: {}", userId);
        return new AuthResponse(
                cached.userId,
                token,
                expireTimestamp,
                cached.userName,
                cached.userMail,
                cached.isPasswordChangeRequired
        );
    }

    public AuthResponse changePassword(String userId, String oldPassword, String newPassword) {
        log.info("Processing changePassword request for userId: {}", userId);

        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (entity.isBlocked()) {
            throw new RuntimeException("Account is blocked.");
        }

        if (!passwordEncryptionService.matches(oldPassword, entity.getPasswordHash())) {
            throw new RuntimeException("Current password does not match.");
        }

        validatePasswordRules(newPassword);

        String newHash = passwordEncryptionService.encryptPassword(newPassword);
        entity.setPasswordHash(newHash);
        entity.setPasswordChangeRequired(false);
        entity.setUpdatedAt(System.currentTimeMillis());

        userRepository.save(entity);

        // Update in-memory cache
        UserCacheDto updatedCache = new UserCacheDto(entity, null);
        userCache.put(userId, updatedCache);

        String token = jwtTokenService.generateToken(entity.getUserId(), entity.getRole());
        long expireTimestamp = jwtTokenService.getExpirationTimestamp();

        log.info("Password successfully changed for userId: {}", userId);
        return new AuthResponse(
                entity.getUserId(),
                token,
                expireTimestamp,
                entity.getUserName(),
                entity.getUserMail(),
                false
        );
    }

    public UserAdminDto registerUser(String userId, String userName, String userMail) {
        log.info("Admin creating user: userId={}, userName={}, userMail={}", userId, userName, userMail);

        if (userRepository.existsById(userId)) {
            throw new RuntimeException("User ID already exists: " + userId);
        }

        String rawTempPassword = PasswordGenerator.generateCompliantPassword();
        String encHash = passwordEncryptionService.encryptPassword(rawTempPassword);

        UserEntity entity = new UserEntity(
                userId.trim(),
                userName.trim(),
                userMail.trim(),
                encHash,
                "USER",
                true, // isPasswordChangeRequired = true
                false,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        userRepository.save(entity);

        UserCacheDto cacheDto = new UserCacheDto(entity, rawTempPassword);
        userCache.put(entity.getUserId(), cacheDto);

        return new UserAdminDto(
                entity.getUserId(),
                entity.getUserName(),
                entity.getUserMail(),
                entity.getRole(),
                true,
                false,
                rawTempPassword, // Send plaintext generated password to admin
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public UserAdminDto updateUser(String userId, String userName, String userMail) {
        log.info("Admin updating user metadata: userId={}, newName={}, newMail={}", userId, userName, userMail);

        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        entity.setUserName(userName.trim());
        entity.setUserMail(userMail.trim());
        entity.setUpdatedAt(System.currentTimeMillis());

        userRepository.save(entity);

        UserCacheDto existing = userCache.get(userId);
        String tempPwd = existing != null ? existing.tempPassword : null;
        UserCacheDto updatedCache = new UserCacheDto(entity, tempPwd);
        userCache.put(userId, updatedCache);

        return new UserAdminDto(
                entity.getUserId(),
                entity.getUserName(),
                entity.getUserMail(),
                entity.getRole(),
                entity.isPasswordChangeRequired(),
                entity.isBlocked(),
                tempPwd,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public String resetPasswordByAdmin(String userId) {
        log.info("Admin resetting password for userId: {}", userId);

        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String rawTempPassword = PasswordGenerator.generateCompliantPassword();
        String encHash = passwordEncryptionService.encryptPassword(rawTempPassword);

        entity.setPasswordHash(encHash);
        entity.setPasswordChangeRequired(true);
        entity.setUpdatedAt(System.currentTimeMillis());

        userRepository.save(entity);

        UserCacheDto updatedCache = new UserCacheDto(entity, rawTempPassword);
        userCache.put(userId, updatedCache);

        return rawTempPassword;
    }

    public void setUserBlockedStatus(String userId, boolean blocked) {
        log.info("Admin setting blocked status for userId: {} -> {}", userId, blocked);

        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        entity.setBlocked(blocked);
        entity.setUpdatedAt(System.currentTimeMillis());

        userRepository.save(entity);

        UserCacheDto existing = userCache.get(userId);
        String tempPwd = existing != null ? existing.tempPassword : null;
        UserCacheDto updatedCache = new UserCacheDto(entity, tempPwd);
        userCache.put(userId, updatedCache);
    }

    public void deleteUser(String userId) {
        log.info("Admin deleting user: {}", userId);
        userRepository.deleteById(userId);
        userCache.remove(userId);
    }

    public List<UserAdminDto> getAllUsersForAdmin() {
        List<UserAdminDto> result = new ArrayList<>();
        for (UserCacheDto cache : userCache.values()) {
            result.add(new UserAdminDto(
                    cache.userId,
                    cache.userName,
                    cache.userMail,
                    cache.role,
                    cache.isPasswordChangeRequired,
                    cache.isBlocked,
                    cache.isPasswordChangeRequired ? cache.tempPassword : null,
                    0L,
                    0L
            ));
        }
        result.sort((a, b) -> a.getUserId().compareTo(b.getUserId()));
        return result;
    }

    private void validatePasswordRules(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long.");
        }
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            throw new RuntimeException("Password must contain at least 1 uppercase letter.");
        }
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            throw new RuntimeException("Password must contain at least 1 lowercase letter.");
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            throw new RuntimeException("Password must contain at least 1 digit.");
        }
        if (!Pattern.compile("[~!@#$%^&*\\-_+=.?:]").matcher(password).find()) {
            throw new RuntimeException("Password must contain at least 1 allowed symbol (~!@#$%^&*-_+=.?:).");
        }
        if (Pattern.compile("\\s").matcher(password).find()) {
            throw new RuntimeException("Password must not contain spaces or tabs.");
        }
    }
}
