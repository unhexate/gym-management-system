package com.gym.repository;

import com.gym.model.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    Optional<WorkoutPlan> findByMemberId(Long memberId);
    List<WorkoutPlan> findByTrainerId(Long trainerId);
    boolean existsByMemberId(Long memberId);
    boolean existsByMemberIdAndTrainerId(Long memberId, Long trainerId);

    @Query("SELECT wp.member.id, wp.trainer.id FROM WorkoutPlan wp")
    List<Object[]> findMemberTrainerAssignments();

    @Query(value = """
            SELECT
                wp.id AS id,
                wp.exercises AS exercises,
                wp.schedule AS schedule,
                wp.difficulty_level AS difficultyLevel,
                m.id AS memberId,
                m.name AS memberName,
                m.email AS memberEmail,
                m.role AS memberRole,
                t.id AS trainerId,
                t.name AS trainerName,
                t.email AS trainerEmail,
                t.role AS trainerRole
            FROM workout_plans wp
            JOIN users m ON wp.member_id = m.id
            JOIN users t ON wp.trainer_id = t.id
            WHERE wp.member_id = :memberId
            LIMIT 1
            """, nativeQuery = true)
    Optional<WorkoutPlanViewProjection> findViewByMemberId(@Param("memberId") Long memberId);

    @Query(value = """
            SELECT
                wp.id AS id,
                wp.exercises AS exercises,
                wp.schedule AS schedule,
                wp.difficulty_level AS difficultyLevel,
                m.id AS memberId,
                m.name AS memberName,
                m.email AS memberEmail,
                m.role AS memberRole,
                t.id AS trainerId,
                t.name AS trainerName,
                t.email AS trainerEmail,
                t.role AS trainerRole
            FROM workout_plans wp
            JOIN users m ON wp.member_id = m.id
            JOIN users t ON wp.trainer_id = t.id
            WHERE wp.member_id = :memberId
              AND wp.trainer_id = :trainerId
            LIMIT 1
            """, nativeQuery = true)
    Optional<WorkoutPlanViewProjection> findViewByMemberIdAndTrainerId(
            @Param("memberId") Long memberId,
            @Param("trainerId") Long trainerId
    );
}
