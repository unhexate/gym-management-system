package com.gym.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "members")
@DiscriminatorValue("MEMBER")
@Getter
@Setter
@NoArgsConstructor
public class Member extends User {

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "status")
    private String status; // ACTIVE / INACTIVE

    public Member(String name, String email, String phone, String password) {
        super(name, email, phone, password);
        this.joinDate = LocalDate.now();
        this.status = "ACTIVE";
    }

    public void enrollMembership() { /* delegates to MembershipService */ }
    public void makePayment() { /* delegates to PaymentService */ }
    public void markAttendance() { /* delegates to AttendanceService */ }
    public void viewWorkoutPlan() { /* delegates to WorkoutService */ }
}
