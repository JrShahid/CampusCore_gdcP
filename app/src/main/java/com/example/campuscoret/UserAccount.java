package com.example.campuscoret;

public class UserAccount {
    private final String name;
    private final String email;
    private final String password;
    private final String role;
    private final String className;

    public UserAccount(String name, String email, String password, String role, String className) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getClassName() {
        return className;
    }
}
