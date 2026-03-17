package com.gym.pattern;

import com.gym.model.*;
import com.gym.service.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Factory Pattern (UserFactory).
 * No Spring context needed – pure logic.
 */
@DisplayName("UserFactory – Factory Pattern")
class UserFactoryTest {

    @Test
    @DisplayName("creates Member when role is MEMBER")
    void createMember() {
        User user = UserFactory.createUser("MEMBER", "Alice", "alice@gym.com", "555-0001", "pass");
        assertInstanceOf(Member.class, user);
        assertEquals("Alice", user.getName());
        assertEquals("alice@gym.com", user.getEmail());
    }

    @Test
    @DisplayName("creates Trainer when role is TRAINER")
    void createTrainer() {
        User user = UserFactory.createUser("TRAINER", "Bob", "bob@gym.com", "555-0002", "pass");
        assertInstanceOf(Trainer.class, user);
    }

    @Test
    @DisplayName("creates Admin when role is ADMIN")
    void createAdmin() {
        User user = UserFactory.createUser("ADMIN", "Carol", "carol@gym.com", "555-0003", "pass");
        assertInstanceOf(Admin.class, user);
    }

    @Test
    @DisplayName("creates Receptionist when role is RECEPTIONIST")
    void createReceptionist() {
        User user = UserFactory.createUser("RECEPTIONIST", "Dave", "dave@gym.com", "555-0004", "pass");
        assertInstanceOf(Receptionist.class, user);
    }

    @Test
    @DisplayName("role matching is case-insensitive")
    void roleIsCaseInsensitive() {
        User lower = UserFactory.createUser("member", "Eve", "eve@gym.com", "555-0005", "pass");
        assertInstanceOf(Member.class, lower);

        User mixed = UserFactory.createUser("Trainer", "Frank", "frank@gym.com", "555-0006", "pass");
        assertInstanceOf(Trainer.class, mixed);
    }

    @Test
    @DisplayName("throws IllegalArgumentException for unknown role")
    void throwsOnUnknownRole() {
        assertThrows(IllegalArgumentException.class,
                () -> UserFactory.createUser("SUPERUSER", "X", "x@gym.com", "000", "pass"));
    }
}
