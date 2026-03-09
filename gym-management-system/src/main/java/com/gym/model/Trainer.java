package com.gym.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trainers")
@DiscriminatorValue("TRAINER")
@Getter
@Setter
@NoArgsConstructor
public class Trainer extends User {

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "experience_years")
    private Integer experienceYears;

    public Trainer(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }

    public Trainer(String name, String email, String phone, String password,
                   String specialization, Integer experienceYears) {
        super(name, email, phone, password);
        this.specialization = specialization;
        this.experienceYears = experienceYears;
    }

    public void createWorkoutPlan() { /* delegates to WorkoutService */ }
    public void updateWorkoutPlan() { /* delegates to WorkoutService */ }
    public void viewAssignedMembers() { /* delegates to WorkoutService */ }
}
