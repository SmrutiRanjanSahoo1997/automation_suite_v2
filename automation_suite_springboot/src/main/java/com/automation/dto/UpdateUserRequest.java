package com.automation.dto;

public class UpdateUserRequest {
    private String userId;
    private String userName;
    private String userMail;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String userId, String userName, String userMail) {
        this.userId = userId;
        this.userName = userName;
        this.userMail = userMail;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }
}
