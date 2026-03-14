package com.gym.controller;

import com.gym.dto.EnrollMembershipRequest;
import com.gym.exception.ApiResponse;
import com.gym.model.Membership;
import com.gym.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /** POST /api/memberships – enroll a member in a plan (Strategy + Template Method) */
    @PostMapping
    public ResponseEntity<ApiResponse<Membership>> enroll(
            @Valid @RequestBody EnrollMembershipRequest request) {
        Membership membership = membershipService.enroll(request.getMemberId(), request.getPlanId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(membership, "Membership enrolled successfully"));
    }

    /** GET /api/memberships/member/{memberId} – get active membership for member */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Membership>> getActiveMembership(
            @PathVariable Long memberId) {
        Membership membership = membershipService.getActiveMembership(memberId);
        return ResponseEntity.ok(ApiResponse.success(membership));
    }
}
