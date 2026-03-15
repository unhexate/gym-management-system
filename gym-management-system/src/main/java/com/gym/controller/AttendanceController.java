package com.gym.controller;

import com.gym.dto.MarkAttendanceRequest;
import com.gym.exception.ApiResponse;
import com.gym.model.Attendance;
import com.gym.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for attendance marking.
 * Endpoint 9: POST /api/attendance
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /** POST /api/attendance – mark a check-in (uses Template Method internally) */
    @PostMapping
    public ResponseEntity<ApiResponse<Attendance>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request) {
        Attendance attendance = attendanceService.markAttendance(
                request.getMemberId(), request.getCheckinTime());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attendance, "Attendance marked successfully"));
    }
}
