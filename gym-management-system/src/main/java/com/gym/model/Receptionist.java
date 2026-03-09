package com.gym.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receptionists")
@DiscriminatorValue("RECEPTIONIST")
@Getter
@Setter
@NoArgsConstructor
public class Receptionist extends User {

    public Receptionist(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }

    public void registerMember() { /* delegates to UserService */ }
    public void collectOfflinePayment() { /* delegates to PaymentService */ }
}
