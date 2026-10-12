package com.automation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    private String userId;
    private String token;
    private long expireTimestamp;
    private String userName;
    private String userMail;

    @JsonProperty("isPasswordChangeRequired")
    private boolean isPasswordChangeRequired;

    public AuthResponse() {}

    public AuthResponse(String userId, String token, long expireTimestamp, String userName, String userMail, boolean isPasswordChangeRequired) {
        this.userId = userId;
        this.token = token;
        this.expireTimestamp = expireTimestamp;
        this.userName = userName;
        this.userMail = userMail;
        this.isPasswordChangeRequired = isPasswordChangeRequired;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public long getExpireTimestamp() { return expireTimestamp; }
    public void setExpireTimestamp(long expireTimestamp) { this.expireTimestamp = expireTimestamp; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    @JsonProperty("isPasswordChangeRequired")
    public boolean isPasswordChangeRequired() { return isPasswordChangeRequired; }

    public void setPasswordChangeRequired(boolean isPasswordChangeRequired) { this.isPasswordChangeRequired = isPasswordChangeRequired; }
}
