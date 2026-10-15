package com.smartcampus.model;

import java.sql.Timestamp;

/**
 * Model representing a Campus System User (Admin, Faculty, Student Coordinator)
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role; // 'ADMIN', 'FACULTY', 'STUDENT_COORDINATOR'
    private String department;
    private String email;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String username, String password, String fullName, String role, String department, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
        this.email = email;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + " - " + department + ")";
    }
}
