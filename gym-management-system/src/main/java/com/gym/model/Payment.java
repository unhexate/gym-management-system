package com.gym.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@Entity
@Table(name = "payments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "payment_mode", nullable = false)
    private String paymentMode; // ONLINE / OFFLINE

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // SUCCESS / FAILED / PENDING
}
