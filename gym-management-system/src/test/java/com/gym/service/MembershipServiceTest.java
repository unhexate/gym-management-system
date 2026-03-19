package com.gym.service;

import com.gym.exception.ResourceNotFoundException;
import com.gym.model.Member;
import com.gym.model.Membership;
import com.gym.model.MembershipPlan;
import com.gym.repository.MemberRepository;
import com.gym.repository.MembershipPlanRepository;
import com.gym.repository.MembershipRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MembershipService")
class MembershipServiceTest {

    @Mock MembershipRepository membershipRepository;
    @Mock MemberRepository memberRepository;
    @Mock MembershipPlanRepository membershipPlanRepository;
    @InjectMocks MembershipService membershipService;

    private Member sampleMember() {
        Member m = new Member("Alice", "alice@gym.com", "123", "pass");
        m.setStatus("ACTIVE");
        return m;
    }

    private MembershipPlan basicPlan() {
        return new MembershipPlan(1L, "BASIC", 1, 100.0, "Basic plan");
    }

    private MembershipPlan premiumPlan() {
        return new MembershipPlan(2L, "PREMIUM", 3, 300.0, "Premium plan");
    }

    @Test
    @DisplayName("enroll saves membership with BasicPricing (no discount)")
    void enrollBasicPlan() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember()));
        when(membershipPlanRepository.findById(1L)).thenReturn(Optional.of(basicPlan()));
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Membership result = membershipService.enroll(1L, 1L);

        assertEquals(100.0, result.getPrice(), 0.001, "BasicPricing should keep base price");
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("enroll applies 20% discount for PREMIUM plan (Strategy Pattern)")
    void enrollPremiumPlan() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember()));
        when(membershipPlanRepository.findById(2L)).thenReturn(Optional.of(premiumPlan()));
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Membership result = membershipService.enroll(1L, 2L);

        assertEquals(240.0, result.getPrice(), 0.001, "PremiumPricing should apply 20% discount");
    }

    @Test
    @DisplayName("enroll throws ResourceNotFoundException for unknown member")
    void enrollUnknownMember() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> membershipService.enroll(99L, 1L));
    }

    @Test
    @DisplayName("enroll throws ResourceNotFoundException for unknown plan")
    void enrollUnknownPlan() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember()));
        when(membershipPlanRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> membershipService.enroll(1L, 99L));
    }

    @Test
    @DisplayName("resolvePricingStrategy returns PremiumPricing for PREMIUM plan name")
    void strategySelectionPremium() {
        PricingStrategy strategy = membershipService.resolvePricingStrategy(premiumPlan());
        assertInstanceOf(PremiumPricing.class, strategy);
    }

    @Test
    @DisplayName("resolvePricingStrategy returns BasicPricing for any non-PREMIUM plan")
    void strategySelectionBasic() {
        PricingStrategy strategy = membershipService.resolvePricingStrategy(basicPlan());
        assertInstanceOf(BasicPricing.class, strategy);
    }

    @Test
    @DisplayName("getActiveMembership throws when no active membership found")
    void getActiveMembershipNotFound() {
        when(membershipRepository.findTopByMemberIdAndStatusOrderByEndDateDesc(1L, "ACTIVE"))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> membershipService.getActiveMembership(1L));
    }
}
