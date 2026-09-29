package com.garagedesk.controller;

import com.garagedesk.dto.request.GoogleAuthRequest;
import com.garagedesk.dto.request.LoginRequest;
import com.garagedesk.dto.request.RegisterRequest;
import com.garagedesk.dto.response.ApiResponse;
import com.garagedesk.dto.response.AuthResponse;
import com.garagedesk.dto.response.UserResponse;
import com.garagedesk.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication & Staff Identity", description = "Endpoints for user login, registration, and Google OAuth flow")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user by username or email")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new staff or client account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.created(response.getMessage(), response), HttpStatus.CREATED);
    }

    @PostMapping("/google")
    @Operation(summary = "Authenticate via Google OAuth token/credentials")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        AuthResponse response = authService.googleAuth(request);
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get user profile by username or email")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@RequestParam String username) {
        UserResponse response = authService.getCurrentUser(username);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Sign out current user session")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", Map.of("status", "LOGGED_OUT")));
    }
}
