package com.gym.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@Entity
@Table(name = "reports")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_type", nullable = false)
    private String reportType; // SUMMARY / REVENUE / ATTENDANCE

    @Column(name = "generated_date", nullable = false)
    private LocalDate generatedDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generated_by")
    private User generatedBy;
}
