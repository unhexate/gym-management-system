package com.gym.controller;

import com.gym.dto.CreateUserRequest;
import com.gym.dto.UpdateProfileRequest;
import com.gym.dto.UserLookupResponse;
import com.gym.exception.ApiResponse;
import com.gym.model.User;
import com.gym.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        String creatorRole = extractRole(authentication);
        User created = userService.registerUser(request, creatorRole);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "User registered successfully"));
    }

    /** GET /api/users/me – returns the currently authenticated user */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> me(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /** GET /api/users/search – lightweight lookup for selector UIs */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserLookupResponse>>> searchUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "20") int limit) {
        List<UserLookupResponse> users = userService.searchUsers(role, q, limit);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /** PUT /api/users/{userId}/profile – update profile fields */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequest request) {
        User updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    private String extractRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse(null);
    }
}
