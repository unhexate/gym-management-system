package com.gym.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@NoArgsConstructor
public class Admin extends User {

    public Admin(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }

    public void addTrainer() { /* delegates to TrainerService */ }
    public void createMembershipPlan() { /* delegates to MembershipService */ }
    public void assignTrainer() { /* delegates to WorkoutService */ }
    public void viewReport() { /* delegates to ReportService */ }
}
