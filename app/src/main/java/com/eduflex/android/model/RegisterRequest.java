package com.eduflex.android.model;

public class RegisterRequest {

    private final String email;
    private final String password;
    private final String name;
    private final boolean active;

    public RegisterRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.active = true;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
