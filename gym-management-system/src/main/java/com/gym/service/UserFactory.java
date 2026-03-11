package com.gym.service;

import com.gym.model.Admin;
import com.gym.model.Member;
import com.gym.model.Receptionist;
import com.gym.model.Trainer;
import com.gym.model.User;

/**
 * Factory Pattern (Creational)
 *
 * Creates the correct User subclass (Admin / Receptionist / Member / Trainer)
 * based on the role string, without exposing instantiation logic to callers.
 */
public class UserFactory {

    private UserFactory() { /* utility class */ }

    public static User createUser(String role, String name, String email,
                                  String phone, String password) {
        return switch (role.toUpperCase().trim()) {
            case "MEMBER"       -> new Member(name, email, phone, password);
            case "TRAINER"      -> new Trainer(name, email, phone, password);
            case "ADMIN"        -> new Admin(name, email, phone, password);
            case "RECEPTIONIST" -> new Receptionist(name, email, phone, password);
            default -> throw new IllegalArgumentException(
                    "Unknown role: '" + role + "'. Valid values: MEMBER, TRAINER, ADMIN, RECEPTIONIST");
        };
    }
}
