package com.garagedesk.service;

import com.garagedesk.dto.request.GoogleAuthRequest;
import com.garagedesk.dto.request.LoginRequest;
import com.garagedesk.dto.request.RegisterRequest;
import com.garagedesk.dto.response.AuthResponse;
import com.garagedesk.dto.response.UserResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse googleAuth(GoogleAuthRequest request);

    UserResponse getCurrentUser(String usernameOrEmail);
}
