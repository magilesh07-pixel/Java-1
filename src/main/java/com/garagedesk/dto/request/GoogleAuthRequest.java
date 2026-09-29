package com.garagedesk.dto.request;

import com.garagedesk.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    private String googleId;

    private String avatarUrl;

    private UserRole role = UserRole.WORKSHOP_MANAGER;

    public GoogleAuthRequest() {
    }

    public GoogleAuthRequest(String email, String name, String googleId, String avatarUrl, UserRole role) {
        this.email = email;
        this.name = name;
        this.googleId = googleId;
        this.avatarUrl = avatarUrl;
        this.role = role != null ? role : UserRole.WORKSHOP_MANAGER;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
