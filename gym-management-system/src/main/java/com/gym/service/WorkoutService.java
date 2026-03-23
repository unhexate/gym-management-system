package com.gym.service;

import com.gym.exception.ResourceNotFoundException;
import com.gym.model.Member;
import com.gym.model.Trainer;
import com.gym.model.WorkoutPlan;

import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Workout plan service – extends {@link BaseCrudService} (Template Method pattern).
 */
@Service
@RequiredArgsConstructor
public class WorkoutService extends BaseCrudService<WorkoutPlan, Long> {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;

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

    public WorkoutPlan getByMemberForTrainer(Long trainerId, Long memberId) {
        WorkoutPlan plan = getByMember(memberId);
        if (plan.getTrainer() == null || !trainerId.equals(plan.getTrainer().getId())) {
            throw new AccessDeniedException("Trainer can only access workout plans for their own members");
        }
        return plan;
    }
}
