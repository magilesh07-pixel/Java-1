package com.garagedesk.service.impl;

import com.garagedesk.dto.request.GoogleAuthRequest;
import com.garagedesk.dto.request.LoginRequest;
import com.garagedesk.dto.request.RegisterRequest;
import com.garagedesk.dto.response.AuthResponse;
import com.garagedesk.dto.response.UserResponse;
import com.garagedesk.entity.AuditLog;
import com.garagedesk.entity.User;
import com.garagedesk.entity.enums.UserRole;
import com.garagedesk.exception.InvalidCredentialsException;
import com.garagedesk.exception.ResourceNotFoundException;
import com.garagedesk.exception.UserAlreadyExistsException;
import com.garagedesk.repository.AuditLogRepository;
import com.garagedesk.repository.UserRepository;
import com.garagedesk.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public AuthServiceImpl(UserRepository userRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String query = request.getUsernameOrEmail().trim();
        User user = userRepository.findByUsernameOrEmail(query, query)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // Match password (plaintext / simple match for academic assessment)
        if (!user.getPassword().equals(request.getPassword().trim())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = "gd_" + UUID.randomUUID().toString().replace("-", "");

        // Log successful authentication in audit trail
        auditLogRepository.save(new AuditLog(
                "User",
                user.getId(),
                "AUTH_LOGIN",
                user.getUsername(),
                "User authenticated successfully into GarageDesk workspace with role: " + user.getRole()
        ));

        return new AuthResponse(token, "Login successful", mapToUserResponse(user));
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username '" + username + "' is already registered");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email '" + email + "' is already registered");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(request.getPassword().trim());
        user.setFullName(request.getFullName().trim());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.WORKSHOP_MANAGER);
        user.setStaffBadgeNumber(request.getStaffBadgeNumber());
        user.setAuthProvider("LOCAL");

        User savedUser = userRepository.save(user);

        // Audit Trail
        auditLogRepository.save(new AuditLog(
                "User",
                savedUser.getId(),
                "AUTH_REGISTER",
                savedUser.getUsername(),
                "New user registered with role: " + savedUser.getRole()
        ));

        String token = "gd_" + UUID.randomUUID().toString().replace("-", "");
        return new AuthResponse(token, "Account created successfully", mapToUserResponse(savedUser));
    }

    @Override
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            String autoUsername = email.contains("@") ? email.split("@")[0] : email;
            if (userRepository.existsByUsername(autoUsername)) {
                autoUsername = autoUsername + "_" + (int) (Math.random() * 1000);
            }
            newUser.setUsername(autoUsername);
            newUser.setEmail(email);
            newUser.setPassword(UUID.randomUUID().toString()); // Secure random password
            newUser.setFullName(request.getName());
            newUser.setRole(request.getRole() != null ? request.getRole() : UserRole.WORKSHOP_MANAGER);
            newUser.setStaffBadgeNumber("GOOGLE-OAUTH");
            newUser.setAuthProvider("GOOGLE");
            newUser.setAvatarUrl(request.getAvatarUrl());
            return userRepository.save(newUser);
        });

        // Audit Trail
        auditLogRepository.save(new AuditLog(
                "User",
                user.getId(),
                "AUTH_GOOGLE",
                user.getUsername(),
                "User authenticated via Google OAuth: " + user.getEmail()
        ));

        String token = "gd_google_" + UUID.randomUUID().toString().replace("-", "");
        return new AuthResponse(token, "Authenticated via Google", mapToUserResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String usernameOrEmail) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + usernameOrEmail));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStaffBadgeNumber(),
                user.getAuthProvider(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}
