package com.secureuniversity.model;

public class User {
    private Long id;
    private String username;
    private String passwordMd5;
    private String role;
    private String email;

    public User() {}
    public User(Long id, String username, String passwordMd5, String role, String email) {
        this.id = id; this.username = username; this.passwordMd5 = passwordMd5; this.role = role; this.email = email;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordMd5() { return passwordMd5; }
    public String getRole() { return role; }
    public String getEmail() { return email; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordMd5(String passwordMd5) { this.passwordMd5 = passwordMd5; }
    public void setRole(String role) { this.role = role; }
    public void setEmail(String email) { this.email = email; }
}
