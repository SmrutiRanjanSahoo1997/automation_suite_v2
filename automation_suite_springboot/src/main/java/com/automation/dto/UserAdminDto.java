package com.automation.dto;

public class UserAdminDto {
    private String userId;
    private String userName;
    private String userMail;
    private String role;
    private boolean isPasswordChangeRequired;
    private boolean isBlocked;
    private String tempPassword; // Populated only if isPasswordChangeRequired == true
    private long createdAt;
    private long updatedAt;

    public UserAdminDto() {}

    public UserAdminDto(String userId, String userName, String userMail, String role, boolean isPasswordChangeRequired, boolean isBlocked, String tempPassword, long createdAt, long updatedAt) {
        this.userId = userId;
        this.userName = userName;
        this.userMail = userMail;
        this.role = role;
        this.isPasswordChangeRequired = isPasswordChangeRequired;
        this.isBlocked = isBlocked;
        this.tempPassword = tempPassword;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isPasswordChangeRequired() { return isPasswordChangeRequired; }
    public void setPasswordChangeRequired(boolean passwordChangeRequired) { isPasswordChangeRequired = passwordChangeRequired; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public String getTempPassword() { return tempPassword; }
    public void setTempPassword(String tempPassword) { this.tempPassword = tempPassword; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
