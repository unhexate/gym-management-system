package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateWorkoutRequest {

    @NotNull(message = "trainerId is required")
    private Long trainerId;

    @NotNull(message = "memberId is required")
    private Long memberId;

    @NotBlank(message = "exercises is required")
    private String exercises;

    @NotBlank(message = "schedule is required")
    private String schedule;

    @NotBlank(message = "difficultyLevel is required")
    private String difficultyLevel; // BEGINNER | INTERMEDIATE | ADVANCED
}
