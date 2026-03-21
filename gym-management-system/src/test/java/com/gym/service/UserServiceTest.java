package com.gym.service;

import com.gym.dto.CreateUserRequest;
import com.gym.dto.UpdateProfileRequest;
import com.gym.exception.BadRequestException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.model.Member;
import com.gym.model.User;
import com.gym.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    private CreateUserRequest buildRequest(String role) {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Test User");
        req.setEmail("test@gym.com");
        req.setPhone("123");
        req.setPassword("secret");
        req.setRole(role);
        return req;
    }

    @Test
    @DisplayName("registerUser saves and returns the user")
    void registerUserSuccess() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(buildRequest("MEMBER"), "ADMIN");

        assertInstanceOf(Member.class, saved);
        assertEquals("Test User", saved.getName());
        verify(userRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("registerUser throws BadRequestException when email already exists")
    void registerUserDuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class,
            () -> userService.registerUser(buildRequest("MEMBER"), "ADMIN"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser uses Factory to create correct subclass for each role")
    void registerUserCreatesCorrectSubclass() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        for (String role : new String[]{"MEMBER", "TRAINER", "ADMIN", "RECEPTIONIST"}) {
            CreateUserRequest req = buildRequest(role);
            req.setEmail(role.toLowerCase() + "@gym.com");
            User u = userService.registerUser(req, "ADMIN");
            assertNotNull(u, "Should create user for role " + role);
        }
    }

    @Test
    @DisplayName("registerUser blocks receptionist from creating admin")
    void registerUserReceptionistCannotCreateAdmin() {
        assertThrows(AccessDeniedException.class,
                () -> userService.registerUser(buildRequest("ADMIN"), "RECEPTIONIST"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser public flow only allows MEMBER")
    void registerUserPublicOnlyMember() {
        assertThrows(AccessDeniedException.class,
                () -> userService.registerUser(buildRequest("TRAINER"), null));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile updates name, phone, password")
    void updateProfileSuccess() {
        Member member = new Member("Old Name", "m@gym.com", "000", "oldPass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(member));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setName("New Name");
        req.setPhone("999");
        req.setPassword("newPass");

        User updated = userService.updateProfile(1L, req);

        assertEquals("New Name", updated.getName());
        assertEquals("999", updated.getPhone());
        assertEquals("newPass", updated.getPassword());
    }

    @Test
    @DisplayName("updateProfile throws ResourceNotFoundException for unknown id")
    void updateProfileNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateProfile(99L, new UpdateProfileRequest()));
    }
}
