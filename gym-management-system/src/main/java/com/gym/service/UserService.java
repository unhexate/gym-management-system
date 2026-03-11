package com.gym.service;

import com.gym.dto.CreateUserRequest;
import com.gym.dto.UpdateProfileRequest;
import com.gym.exception.BadRequestException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.model.User;

import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public User registerUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }
        // Factory Pattern: create the right User subclass without exposing constructors
        User user = UserFactory.createUser(
                request.getRole(),
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
}
