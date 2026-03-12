package com.gym.service;

import com.gym.exception.ResourceNotFoundException;
import com.gym.model.Member;
import com.gym.model.Membership;
import com.gym.model.Payment;

import com.gym.repository.MemberRepository;
import com.gym.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Payment service – demonstrates Template Method pattern via {@link BaseCrudService}.
 */
@Service
@RequiredArgsConstructor
public class PaymentService extends BaseCrudService<Payment, Long> {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    // -------------------------------------------------------------------------
    // Template Method – abstract step implementations
    // -------------------------------------------------------------------------

    @Override
    protected Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    protected Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    @Override
    protected void performDelete(Long id) {
        paymentRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Template Method – hook overrides
    // -------------------------------------------------------------------------

    @Override
    protected void validate(Payment payment) {
        if (payment.getMember() == null) {
            throw new IllegalArgumentException("Payment must be linked to a member");
        }
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }

    @Override
    protected void beforeSave(Payment payment) {
        if (payment.getDate() == null) {
            payment.setDate(LocalDate.now());
        }
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus("SUCCESS");
        }
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Process a payment. Uses template {@code create()} (validate → beforeSave → save).
     */
    @Transactional
    public Payment process(Long memberId, Membership membership, Double amount, String paymentMode) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        Payment payment = new Payment();
        payment.setMember(member);
        payment.setMembership(membership);
        payment.setAmount(amount);
        payment.setPaymentMode(paymentMode.toUpperCase());
        payment.setDate(LocalDate.now());
        payment.setPaymentStatus("SUCCESS");

        return create(payment); // delegate to Template Method
    }

    public List<Payment> getPaymentHistory(Long memberId) {
        return paymentRepository.findByMemberIdOrderByDateDesc(memberId);
    }

    public Double getTotalRevenue() {
        return paymentRepository.sumSuccessfulPayments();
    }
}
