package com.gym.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(name = "role", insertable = false, updatable = false)
    private String role;

    public User(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    // Methods from class diagram
    public void login() { /* handled by authentication layer */ }
    public void logout() { /* handled by authentication layer */ }

    public void updateProfile(String name, String phone, String password) {
        if (name != null && !name.isBlank()) this.name = name;
        if (phone != null && !phone.isBlank()) this.phone = phone;
        if (password != null && !password.isBlank()) this.password = password;
    }
}
