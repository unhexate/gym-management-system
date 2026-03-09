package com.gym.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "membership_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_name", nullable = false, unique = true)
    private String planName; // e.g. "BASIC", "PREMIUM"

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(nullable = false)
    private Double price;

    @Column
    private String description;
}
