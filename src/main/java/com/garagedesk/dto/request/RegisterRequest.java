package com.garagedesk.dto.request;

import com.garagedesk.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private UserRole role = UserRole.WORKSHOP_MANAGER;

    private String staffBadgeNumber;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String email, String password, String fullName, UserRole role, String staffBadgeNumber) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role != null ? role : UserRole.WORKSHOP_MANAGER;
        this.staffBadgeNumber = staffBadgeNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getStaffBadgeNumber() {
        return staffBadgeNumber;
    }

    public void setStaffBadgeNumber(String staffBadgeNumber) {
        this.staffBadgeNumber = staffBadgeNumber;
    }
}
