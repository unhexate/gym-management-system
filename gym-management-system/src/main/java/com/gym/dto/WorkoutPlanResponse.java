package com.gym.dto;

import com.gym.repository.WorkoutPlanViewProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkoutPlanResponse {
    private Long id;
    private UserLookupResponse trainer;
    private UserLookupResponse member;
    private String exercises;
    private String schedule;
    private String difficultyLevel;

    public static WorkoutPlanResponse from(WorkoutPlanViewProjection p) {
        return new WorkoutPlanResponse(
                p.getId(),
                new UserLookupResponse(p.getTrainerId(), p.getTrainerName(), p.getTrainerEmail(), p.getTrainerRole()),
                new UserLookupResponse(p.getMemberId(), p.getMemberName(), p.getMemberEmail(), p.getMemberRole()),
                p.getExercises(),
                p.getSchedule(),
                p.getDifficultyLevel()
        );
    }
}
