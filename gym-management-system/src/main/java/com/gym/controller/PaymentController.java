package com.gym.controller;

import com.gym.dto.ProcessPaymentRequest;
import com.gym.dto.SubmitPaymentRequest;
import com.gym.dto.UpdatePaymentStatusRequest;
import com.gym.exception.ApiResponse;
import com.gym.exception.BadRequestException;
import com.gym.model.Membership;
import com.gym.model.Payment;
import com.gym.model.User;
import com.gym.service.MembershipService;
import com.gym.service.PaymentService;
import com.gym.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    private final UserService userService;

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

    /** POST /api/payments/request – member submits payment request for review */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Payment>> submitPaymentRequest(
            @Valid @RequestBody SubmitPaymentRequest request,
            Principal principal) {
        User user = userService.findByEmail(principal.getName());
        if (!"MEMBER".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Only members can submit payment requests");
        }

        Membership membership = membershipService.getById(request.getMembershipId());
        Payment payment = paymentService.submitPaymentRequest(
                user.getId(), membership, request.getAmount(), request.getPaymentMode());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment, "Payment request submitted successfully"));
    }

    /** GET /api/payments/pending – staff gets pending payment requests */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Payment>>> getPendingPaymentRequests() {
        List<Payment> payments = paymentService.getPendingRequests();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /** PUT /api/payments/{paymentId}/status – staff approves/rejects payment request */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<Payment>> updatePaymentStatus(
            @PathVariable Long paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        Payment payment = paymentService.updateStatus(paymentId, request.getPaymentStatus());
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment status updated successfully"));
    }

    /** GET /api/payments/member/{memberId} – full payment history for a member */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentHistory(
            @PathVariable Long memberId) {
        List<Payment> payments = paymentService.getPaymentHistory(memberId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /** GET /api/payments/me – payment history for currently authenticated member */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Payment>>> getMyPaymentHistory(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        if (!"MEMBER".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Only members can use /api/payments/me");
        }

        List<Payment> payments = paymentService.getPaymentHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
