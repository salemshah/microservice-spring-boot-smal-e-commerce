package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1 Authentication", description = "User registration, login, and account management endpoints")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequestDTO request) {
        User createdUser = userService.registerUser(request);
        UserResponseDTO responseDTO = userMapper.toAuthResponseDTO(createdUser);
        return buildResponse(HttpStatus.CREATED, "User registered successfully", responseDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user and issue access + refresh tokens")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO request) {
        Map<String, Object> response = userService.loginAndGenerateTokens(request);
        return buildResponse(HttpStatus.OK, "You are logged successfully", response);
    }


    @Operation(summary = "Refresh JWT access token using a valid refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        Map<String, Object> response = userService.refreshAccessToken(request.getRefreshToken());
        return buildResponse(HttpStatus.OK, "Access token refreshed successfully", response);
    }


    @Operation(summary = "Get logged-in user profile")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(userMapper.toResponseDTO(currentUser));
    }

    @Operation(summary = "Update logged-in user profile")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(@Valid @RequestBody UserRequestDTO dto) {
        User updated = userService.updateCurrentUser(dto);
        return ResponseEntity.ok(userMapper.toResponseDTO(updated));
    }

    @Operation(summary = "Delete current user account")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteMyAccount() {
        String message = userService.deleteCurrentUser();
        return ResponseEntity.ok(Map.of("message", message));
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("message", message);
        if (data != null) response.put("data", data);
        return ResponseEntity.status(status).body(response);
    }
}
