package com.gym.service;

import com.gym.dto.WorkoutPlanResponse;
import com.gym.exception.ResourceNotFoundException;
import com.gym.dto.UserLookupResponse;
import com.gym.model.Member;
import com.gym.model.Trainer;
import com.gym.model.WorkoutPlan;

import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.UserRepository;
import com.gym.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Workout plan service – extends {@link BaseCrudService} (Template Method pattern).
 */
@Service
@RequiredArgsConstructor
public class WorkoutService extends BaseCrudService<WorkoutPlan, Long> {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // Template Method – abstract step implementations
    // -------------------------------------------------------------------------

    @Override
    protected WorkoutPlan save(WorkoutPlan workoutPlan) {
        return workoutPlanRepository.save(workoutPlan);
    }

    @Override
    protected WorkoutPlan findById(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", id));
    }

    @Override
    protected void performDelete(Long id) {
        workoutPlanRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Template Method – hook overrides
    // -------------------------------------------------------------------------

    @Override
    protected void validate(WorkoutPlan workoutPlan) {
        if (workoutPlan.getTrainer() == null) {
            throw new IllegalArgumentException("WorkoutPlan must have a trainer");
        }
        if (workoutPlan.getMember() == null) {
            throw new IllegalArgumentException("WorkoutPlan must have a member");
        }
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    @Transactional
    public WorkoutPlan createPlan(Long trainerId, Long memberId,
                                   String exercises, String schedule, String difficultyLevel) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", trainerId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        Optional<WorkoutPlan> existingPlanOpt = workoutPlanRepository.findByMemberId(memberId);
        if (existingPlanOpt.isPresent()) {
            WorkoutPlan existingPlan = existingPlanOpt.get();
            if (existingPlan.getTrainer() == null || !trainerId.equals(existingPlan.getTrainer().getId())) {
                throw new AccessDeniedException("Member is already assigned to another trainer");
            }

            existingPlan.setExercises(exercises);
            existingPlan.setSchedule(schedule);
            existingPlan.setDifficultyLevel(difficultyLevel.toUpperCase());
            return save(existingPlan);
        }

        WorkoutPlan plan = new WorkoutPlan();
        plan.setTrainer(trainer);
        plan.setMember(member);
        plan.setExercises(exercises);
        plan.setSchedule(schedule);
        plan.setDifficultyLevel(difficultyLevel.toUpperCase());

        return create(plan); // delegate to Template Method
    }

    public WorkoutPlan getByMember(Long memberId) {
        return workoutPlanRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No workout plan found for member id: " + memberId));
    }

        public WorkoutPlanResponse getViewByMember(Long memberId) {
        return workoutPlanRepository.findViewByMemberId(memberId)
            .map(WorkoutPlanResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No workout plan found for member id: " + memberId));
        }

    public WorkoutPlan getByMemberForTrainer(Long trainerId, Long memberId) {
        WorkoutPlan plan = getByMember(memberId);
        if (plan.getTrainer() == null || !trainerId.equals(plan.getTrainer().getId())) {
            throw new AccessDeniedException("Trainer can only access workout plans for their own members");
        }
        return plan;
    }

    public WorkoutPlanResponse getViewByMemberForTrainer(Long trainerId, Long memberId) {
        if (!workoutPlanRepository.existsByMemberId(memberId)) {
            throw new ResourceNotFoundException("No workout plan found for member id: " + memberId);
        }
        if (!workoutPlanRepository.existsByMemberIdAndTrainerId(memberId, trainerId)) {
            throw new AccessDeniedException("Trainer can only access workout plans for their own members");
        }

        return workoutPlanRepository.findViewByMemberIdAndTrainerId(memberId, trainerId)
                .map(WorkoutPlanResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found"));
    }

        public List<UserLookupResponse> getManageableMembersForTrainer(Long trainerId) {
        Map<Long, Long> assignedTrainerByMemberId = workoutPlanRepository.findMemberTrainerAssignments().stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1],
                        (first, second) -> second
                ));

        return userRepository.findLookupByRole("MEMBER").stream()
            .filter(member -> {
                Long assignedTrainerId = assignedTrainerByMemberId.get(member.getId());
                    return assignedTrainerId == null || trainerId.equals(assignedTrainerId);
                })
            .map(UserLookupResponse::from)
            .sorted(Comparator.comparing(UserLookupResponse::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public boolean isMemberAssignedToTrainer(Long trainerId, Long memberId) {
        return workoutPlanRepository.findByMemberId(memberId)
                .map(plan -> plan.getTrainer() != null && trainerId.equals(plan.getTrainer().getId()))
                .orElse(false);
    }
}
