package com.gym.repository;

import com.gym.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByMemberIdOrderByDateDesc(Long memberId);
    Optional<Attendance> findTopByMemberIdAndDateAndCheckoutTimeIsNullOrderByCheckinTimeDesc(Long memberId, LocalDate date);
    long countByMemberId(Long memberId);
}
