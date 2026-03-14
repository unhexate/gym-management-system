package com.gym.controller;

import com.gym.dto.ProcessPaymentRequest;
import com.gym.exception.ApiResponse;
import com.gym.model.Membership;
import com.gym.model.Payment;
import com.gym.service.MembershipService;
import com.gym.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for payment processing and history.
 * Endpoint 5: POST /api/payments        (uses Facade Pattern)
 * Endpoint 6: GET  /api/payments/member/{memberId}
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MembershipService membershipService;

    /** POST /api/payments – process a payment (Facade coordinates membership + payment) */
    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request) {
        Membership membership = membershipService.getById(request.getMembershipId());
        Payment payment = paymentService.process(
                request.getMemberId(), membership, request.getAmount(), request.getPaymentMode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment, "Payment processed successfully"));
    }

    /** GET /api/payments/member/{memberId} – full payment history for a member */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentHistory(
            @PathVariable Long memberId) {
        List<Payment> payments = paymentService.getPaymentHistory(memberId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
