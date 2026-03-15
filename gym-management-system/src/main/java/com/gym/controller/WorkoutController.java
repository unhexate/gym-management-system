package com.gym.controller;

import com.gym.dto.CreateWorkoutRequest;
import com.gym.exception.ApiResponse;
import com.gym.model.WorkoutPlan;
import com.gym.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for workout plan management.
 * Endpoint 7: POST /api/workouts
 * Endpoint 8: GET  /api/workouts/member/{memberId}
 */
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    /** POST /api/workouts – trainer creates a workout plan for a member */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutPlan>> createWorkout(
            @Valid @RequestBody CreateWorkoutRequest request) {
        WorkoutPlan plan = workoutService.createPlan(
                request.getTrainerId(),
                request.getMemberId(),
                request.getExercises(),
                request.getSchedule(),
                request.getDifficultyLevel()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(plan, "Workout plan created successfully"));
    }

    /** GET /api/workouts/member/{memberId} – member views their workout schedule */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<WorkoutPlan>> getWorkout(@PathVariable Long memberId) {
        WorkoutPlan plan = workoutService.getByMember(memberId);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }
}
