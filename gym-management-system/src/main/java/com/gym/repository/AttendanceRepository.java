package com.gym.repository;

import com.gym.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByMemberIdOrderByDateDesc(Long memberId);
    long countByMemberId(Long memberId);
}
