package com.gym.controller;

import com.gym.exception.ApiResponse;
import com.gym.service.GymManagementFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for admin reports.
 * Endpoint 10: GET /api/reports  (uses Facade Pattern to aggregate data)
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final GymManagementFacade gymManagementFacade;

    /** GET /api/reports – admin views system-wide summary (Facade aggregates sub-services) */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReport() {
        Map<String, Object> report = gymManagementFacade.generateReport();
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
