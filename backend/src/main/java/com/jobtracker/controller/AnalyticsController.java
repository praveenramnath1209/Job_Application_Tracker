package com.jobtracker.controller;

import com.jobtracker.dto.common.ApiResponse;
import com.jobtracker.dto.response.AnalyticsResponse;
import com.jobtracker.service.AnalyticsService;
import com.jobtracker.util.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getSummary() {
        UUID userId = currentUserResolver.getCurrentUserId();
        AnalyticsResponse result = analyticsService.getSummary(userId);
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved", result));
    }
}
