package com.gym.repository;

public interface WorkoutPlanViewProjection {
    Long getId();
    String getExercises();
    String getSchedule();
    String getDifficultyLevel();

    Long getMemberId();
    String getMemberName();
    String getMemberEmail();
    String getMemberRole();

    Long getTrainerId();
    String getTrainerName();
    String getTrainerEmail();
    String getTrainerRole();
}
