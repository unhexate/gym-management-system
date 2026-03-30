package com.gym.repository;

import com.gym.model.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    Optional<WorkoutPlan> findByMemberId(Long memberId);
    List<WorkoutPlan> findByTrainerId(Long trainerId);

    @Query("SELECT wp.member.id, wp.trainer.id FROM WorkoutPlan wp")
    List<Object[]> findMemberTrainerAssignments();
}
