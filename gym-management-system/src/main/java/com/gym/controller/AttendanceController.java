package com.gym.controller;

import com.gym.dto.MarkCheckoutRequest;
import com.gym.dto.MarkAttendanceRequest;
import com.gym.exception.ApiResponse;
import com.gym.exception.BadRequestException;
import com.gym.model.Attendance;
import com.gym.model.User;
import com.gym.service.AttendanceService;
import com.gym.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST controller for attendance marking.
 * Endpoint 9: POST /api/attendance
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
        private final UserService userService;

    /** POST /api/attendance – mark a check-in (uses Template Method internally) */
    @PostMapping
    public ResponseEntity<ApiResponse<Attendance>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request) {
        Attendance attendance = attendanceService.markAttendance(
                request.getMemberId(), request.getCheckinTime());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attendance, "Attendance marked successfully"));
    }

    /** PUT /api/attendance/checkout – mark check-out for today's open attendance */
    @PutMapping("/checkout")
    public ResponseEntity<ApiResponse<Attendance>> markCheckout(
            @Valid @RequestBody MarkCheckoutRequest request) {
        Attendance attendance = attendanceService.markCheckout(
                request.getMemberId(), request.getCheckoutTime());
        return ResponseEntity.ok(ApiResponse.success(attendance, "Checkout marked successfully"));
    }

        /** GET /api/attendance/member/{memberId} – attendance history for a member */
        @GetMapping("/member/{memberId}")
        public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceByMember(
                        @PathVariable Long memberId) {
                List<Attendance> attendance = attendanceService.getAttendanceByMember(memberId);
                return ResponseEntity.ok(ApiResponse.success(attendance));
        }

        /** GET /api/attendance/me – attendance history for currently authenticated member */
        @GetMapping("/me")
        public ResponseEntity<ApiResponse<List<Attendance>>> getMyAttendance(Principal principal) {
                User user = userService.findByEmail(principal.getName());
                if (!"MEMBER".equalsIgnoreCase(user.getRole())) {
                        throw new BadRequestException("Only members can use /api/attendance/me");
                }

                List<Attendance> attendance = attendanceService.getAttendanceByMember(user.getId());
                return ResponseEntity.ok(ApiResponse.success(attendance));
        }
}
