package com.jobtracker.controller;

import com.jobtracker.dto.common.ApiResponse;
import com.jobtracker.dto.common.PagedResponse;
import com.jobtracker.dto.request.CreateApplicationRequest;
import com.jobtracker.dto.request.UpdateApplicationRequest;
import com.jobtracker.dto.request.UpdateStatusRequest;
import com.jobtracker.dto.response.ApplicationResponse;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.util.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> getApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = currentUserResolver.getCurrentUserId();
        PagedResponse<ApplicationResponse> result = applicationService.getApplications(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved", result));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getAllApplications() {
        UUID userId = currentUserResolver.getCurrentUserId();
        List<ApplicationResponse> result = applicationService.getAllApplications(userId);
        return ResponseEntity.ok(ApiResponse.success("All applications retrieved", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(@PathVariable UUID id) {
        UUID userId = currentUserResolver.getCurrentUserId();
        ApplicationResponse result = applicationService.getApplicationById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Application retrieved", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @Valid @RequestBody CreateApplicationRequest request) {
        UUID userId = currentUserResolver.getCurrentUserId();
        ApplicationResponse result = applicationService.createApplication(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application created successfully", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationRequest request) {
        UUID userId = currentUserResolver.getCurrentUserId();
        ApplicationResponse result = applicationService.updateApplication(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Application updated successfully", result));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        UUID userId = currentUserResolver.getCurrentUserId();
        ApplicationResponse result = applicationService.updateStatus(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(@PathVariable UUID id) {
        UUID userId = currentUserResolver.getCurrentUserId();
        applicationService.deleteApplication(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Application deleted successfully"));
    }
}
