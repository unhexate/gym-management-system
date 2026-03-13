package com.gym.controller;

import com.gym.dto.CreateUserRequest;
import com.gym.dto.UpdateProfileRequest;
import com.gym.exception.ApiResponse;
import com.gym.model.User;
import com.gym.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user registration and profile management.
 * Endpoint 1: POST  /api/users
 * Endpoint 2: PUT   /api/users/{userId}/profile
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** POST /api/users – register a new user (uses Factory Pattern internally) */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> registerUser(
            @Valid @RequestBody CreateUserRequest request) {
        User created = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "User registered successfully"));
    }

    /** PUT /api/users/{userId}/profile – update profile fields */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequest request) {
        User updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }
}
