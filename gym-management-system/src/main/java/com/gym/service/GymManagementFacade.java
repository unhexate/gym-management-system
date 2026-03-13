package com.gym.service;

import com.gym.model.Membership;
import com.gym.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Facade Pattern (Structural)
 *
 * Provides a single simplified entry point that coordinates interactions between
 * MembershipService, PaymentService, and ReportService.
 *
 * Callers never need to know how these subsystems interact internally,
 * which reduces coupling and complexity (Separation of Concerns principle).
 */
@Service
@RequiredArgsConstructor
public class GymManagementFacade {

    private final MembershipService membershipService;
    private final PaymentService paymentService;
    private final ReportService reportService;

    /**
     * Enroll a member in a plan AND immediately process the payment in one call.
     *
     * Internally coordinates:
     * 1. MembershipService.enroll()  – creates membership (uses Strategy + Template Method)
     * 2. PaymentService.process()    – records payment (uses Template Method)
     *
     * @return the recorded {@link Payment}
     */
    @Transactional
    public Payment enrollAndPay(Long memberId, Long planId, String paymentMode) {
        Membership membership = membershipService.enroll(memberId, planId);
        return paymentService.process(memberId, membership, membership.getPrice(), paymentMode);
    }

    /**
     * Generate a system-wide summary report.
     *
     * Internally aggregates data from MembershipService and ReportService.
     *
     * @return map of report fields (totalMembers, activeMembers, totalRevenue, etc.)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateReport() {
        return reportService.generateSummaryReport();
    }
}
