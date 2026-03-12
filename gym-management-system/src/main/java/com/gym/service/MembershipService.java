package com.gym.service;

import com.gym.exception.ResourceNotFoundException;
import com.gym.model.Member;
import com.gym.model.Membership;
import com.gym.model.MembershipPlan;
import com.gym.repository.MemberRepository;
import com.gym.repository.MembershipPlanRepository;
import com.gym.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Membership service – demonstrates:
 * <ul>
 *   <li><b>Template Method pattern</b>: extends {@link BaseCrudService} for CRUD skeleton</li>
 *   <li><b>Strategy pattern</b>: selects {@link PricingStrategy} at runtime based on plan name</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MembershipService extends BaseCrudService<Membership, Long> {

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository membershipPlanRepository;

    // -------------------------------------------------------------------------
    // Template Method – abstract step implementations
    // -------------------------------------------------------------------------

    @Override
    protected Membership save(Membership membership) {
        return membershipRepository.save(membership);
    }

    @Override
    protected Membership findById(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", id));
    }

    @Override
    protected void performDelete(Long id) {
        membershipRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Template Method – hook overrides
    // -------------------------------------------------------------------------

    @Override
    protected void validate(Membership membership) {
        if (membership.getMember() == null) {
            throw new IllegalArgumentException("Membership must be linked to a member");
        }
        if (membership.getPlan() == null) {
            throw new IllegalArgumentException("Membership must reference a plan");
        }
    }

    /**
     * beforeSave hook: apply Strategy pattern to compute the final price.
     */
    @Override
    protected void beforeSave(Membership membership) {
        PricingStrategy strategy = resolvePricingStrategy(membership.getPlan());
        double finalPrice = strategy.calculatePrice(membership.getPlan().getPrice());
        membership.setPrice(finalPrice);
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Enroll a member in a plan. Uses template {@code create()} which internally
     * calls validate → beforeSave (applies pricing strategy) → save.
     */
    @Transactional
    public Membership enroll(Long memberId, Long planId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));
        MembershipPlan plan = membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", planId));

        Membership membership = new Membership();
        membership.setMember(member);
        membership.setPlan(plan);
        membership.setStartDate(LocalDate.now());
        membership.setEndDate(LocalDate.now().plusMonths(plan.getDurationMonths()));
        membership.setStatus("ACTIVE");
        membership.setPrice(plan.getPrice()); // will be overwritten in beforeSave

        return create(membership); // delegate to Template Method
    }

    public Membership getActiveMembership(Long memberId) {
        return membershipRepository
                .findTopByMemberIdAndStatusOrderByEndDateDesc(memberId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active membership found for member id: " + memberId));
    }

    public List<Membership> getAllByMember(Long memberId) {
        return membershipRepository.findByMemberId(memberId);
    }

    // Expose findById publicly for use by other services
    public Membership getById(Long id) {
        return findById(id);
    }

    /** Total number of members regardless of status. */
    public long countAllMembers() {
        return memberRepository.count();
    }

    /** Number of members with ACTIVE status. */
    public long countActiveMembers() {
        return memberRepository.countByStatus("ACTIVE");
    }

    // -------------------------------------------------------------------------
    // Strategy selection
    // -------------------------------------------------------------------------

    /**
     * Strategy Pattern: choose the correct pricing strategy based on plan name.
     * New plan types can be added here without modifying existing strategies (OCP).
     */
    PricingStrategy resolvePricingStrategy(MembershipPlan plan) {
        if (plan.getPlanName().equalsIgnoreCase("PREMIUM")) {
            return new PremiumPricing();
        }
        return new BasicPricing();
    }
}
