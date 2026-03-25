package com.gym.controller;

import com.gym.dto.EnrollMembershipRequest;
import com.gym.dto.MembershipPurchaseResponse;
import com.gym.dto.PurchaseMembershipRequest;
import com.gym.dto.SelfEnrollMembershipRequest;
import com.gym.exception.ApiResponse;
import com.gym.exception.BadRequestException;
import com.gym.model.Membership;
import com.gym.model.MembershipPlan;
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
 * REST controller for membership enrollment and status.
 * Endpoint 3: POST /api/memberships
 * Endpoint 4: GET  /api/memberships/member/{memberId}
 */
@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;
    private final PaymentService paymentService;
    private final UserService userService;

    /** POST /api/memberships – enroll a member in a plan (Strategy + Template Method) */
    @PostMapping
    public ResponseEntity<ApiResponse<Membership>> enroll(
            @Valid @RequestBody EnrollMembershipRequest request) {
        Membership membership = membershipService.enroll(request.getMemberId(), request.getPlanId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(membership, "Membership enrolled successfully"));
    }

    /** POST /api/memberships/me – authenticated member enrolls themselves in a plan */
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<Membership>> selfEnroll(
            @Valid @RequestBody SelfEnrollMembershipRequest request,
            Principal principal) {
        User user = userService.findByEmail(principal.getName());
        if (!"MEMBER".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Only members can self-enroll memberships");
        }

        Membership membership = membershipService.enroll(user.getId(), request.getPlanId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(membership, "Membership purchased successfully"));
    }

    /** POST /api/memberships/me/purchase – member buys plan and creates payment request */
    @PostMapping("/me/purchase")
    public ResponseEntity<ApiResponse<MembershipPurchaseResponse>> purchaseMembership(
            @Valid @RequestBody PurchaseMembershipRequest request,
            Principal principal) {
        User user = userService.findByEmail(principal.getName());
        if (!"MEMBER".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Only members can purchase memberships");
        }

        Membership membership = membershipService.enroll(user.getId(), request.getPlanId());
        Payment payment = paymentService.submitPaymentRequest(
                user.getId(), membership, membership.getPrice(), request.getPaymentMode());

        MembershipPurchaseResponse response = new MembershipPurchaseResponse(membership, payment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Membership purchase initiated successfully"));
    }

    /** GET /api/memberships/member/{memberId} – get active membership for member */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Membership>> getActiveMembership(
            @PathVariable Long memberId) {
        Membership membership = membershipService.getActiveMembership(memberId);
        return ResponseEntity.ok(ApiResponse.success(membership));
    }

    /** GET /api/memberships/me – get active membership for currently authenticated member */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Membership>> getMyActiveMembership(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        if (!"MEMBER".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Only members can use /api/memberships/me");
        }

        Membership membership = membershipService.getActiveMembership(user.getId());
        return ResponseEntity.ok(ApiResponse.success(membership));
    }

    /** GET /api/memberships/plans – list available plans for selector UIs */
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<MembershipPlan>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success(membershipService.getAllPlans()));
    }
}
