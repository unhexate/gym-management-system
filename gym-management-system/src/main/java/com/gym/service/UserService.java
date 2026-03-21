package com.gym.service;

import com.gym.dto.CreateUserRequest;
import com.gym.dto.UpdateProfileRequest;
import com.gym.exception.BadRequestException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.model.User;

import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * User management service.
 * Uses the Factory Pattern to instantiate the correct User subclass.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Register a new user.
     * Delegates object creation to {@link UserFactory} (Factory Pattern).
     */
    @Transactional
    public User registerUser(CreateUserRequest request, String creatorRole) {
        String targetRole = normalizeRole(request.getRole());
        validateCreatorRolePolicy(targetRole, creatorRole);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Factory Pattern: create the right User subclass without exposing constructors
        User user = UserFactory.createUser(
                targetRole,
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword()
        );
        return userRepository.save(user);
    }

    /**
     * Update a user's profile fields.
     */
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.updateProfile(request.getName(), request.getPhone(), request.getPassword());
        return userRepository.save(user);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new BadRequestException("role is required");
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private void validateCreatorRolePolicy(String targetRole, String creatorRole) {
        if (creatorRole == null || creatorRole.isBlank()) {
            if (!"MEMBER".equals(targetRole)) {
                throw new AccessDeniedException("Public registration can only create MEMBER accounts");
            }
            return;
        }

        String normalizedCreatorRole = creatorRole.trim().toUpperCase(Locale.ROOT);

        if ("ADMIN".equals(normalizedCreatorRole)) {
            return;
        }

        if ("RECEPTIONIST".equals(normalizedCreatorRole)) {
            if (!"MEMBER".equals(targetRole)) {
                throw new AccessDeniedException("Receptionist can only create MEMBER accounts");
            }
            return;
        }

        throw new AccessDeniedException("Only ADMIN or RECEPTIONIST can create users while authenticated");
    }
}
