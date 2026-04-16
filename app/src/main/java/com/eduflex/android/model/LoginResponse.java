package com.eduflex.android.model;

public class LoginResponse {

    private boolean success;
    private String message;
    private String token;
    private String role;
    private String fullName;
    private String email;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public String getRole() { return role != null ? role : "user"; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
}
