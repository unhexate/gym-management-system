package com.gym.controller;

import com.gym.dto.CreateWorkoutRequest;
import com.gym.dto.UserLookupResponse;
import com.gym.exception.ApiResponse;
import com.gym.exception.BadRequestException;
import com.gym.model.User;
import com.gym.model.WorkoutPlan;
import com.gym.service.UserService;
import com.gym.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
    private final UserService userService;

    /** POST /api/workouts – trainer creates a workout plan for a member */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutPlan>> createWorkout(
            @Valid @RequestBody CreateWorkoutRequest request,
            Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        if ("TRAINER".equalsIgnoreCase(currentUser.getRole())
                && !currentUser.getId().equals(request.getTrainerId())) {
            throw new AccessDeniedException("Trainer can only create workout plans for their own trainerId");
        }

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
    public ResponseEntity<ApiResponse<WorkoutPlan>> getWorkout(
            @PathVariable Long memberId,
            Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());

        WorkoutPlan plan;
        if ("TRAINER".equalsIgnoreCase(currentUser.getRole())) {
            plan = workoutService.getByMemberForTrainer(currentUser.getId(), memberId);
        } else {
            plan = workoutService.getByMember(memberId);
        }

        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    /** GET /api/workouts/me – member views their own workout plan */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WorkoutPlan>> getMyWorkout(Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        WorkoutPlan plan = workoutService.getByMember(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    /** GET /api/workouts/manageable-members – trainer gets members they can manage */
    @GetMapping("/manageable-members")
    public ResponseEntity<ApiResponse<List<UserLookupResponse>>> getManageableMembers(Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        if (!"TRAINER".equalsIgnoreCase(currentUser.getRole())) {
            throw new BadRequestException("Only trainers can use /api/workouts/manageable-members");
        }

        List<UserLookupResponse> members = workoutService.getManageableMembersForTrainer(currentUser.getId())
                .stream()
                .map(UserLookupResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(members));
    }
}
