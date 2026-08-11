package com.registration.dto;

import com.registration.entity.User;

public class AdminUpdateUserRequest {

    private String username;
    private String email;

    private User.Role role;

    public AdminUpdateUserRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }
}