package com.gym.service;

import com.gym.repository.AttendanceRepository;
import com.gym.repository.MemberRepository;
import com.gym.repository.PaymentRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Report service – aggregates statistics from multiple repositories.
 * Used by {@link GymManagementFacade} (Facade pattern).
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;

    /**
     * Build a summary report aggregating data across the system.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateSummaryReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalMembers", memberRepository.count());
        report.put("activeMembers", memberRepository.countByStatus("ACTIVE"));
        report.put("totalRevenue", paymentRepository.sumSuccessfulPayments());
        report.put("totalAttendanceSessions", attendanceRepository.count());
        return report;
    }
}
