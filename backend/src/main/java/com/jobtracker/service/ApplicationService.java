package com.jobtracker.service;

import com.jobtracker.dto.common.PagedResponse;
import com.jobtracker.dto.request.CreateApplicationRequest;
import com.jobtracker.dto.request.UpdateApplicationRequest;
import com.jobtracker.dto.request.UpdateStatusRequest;
import com.jobtracker.dto.response.ApplicationResponse;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.exception.UnauthorizedException;
import com.jobtracker.mapper.ApplicationMapper;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> getApplications(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<JobApplication> applications = applicationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<ApplicationResponse> responses = applications.getContent()
                .stream()
                .map(ApplicationMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(applications, responses);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications(UUID userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ApplicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(UUID id, UUID userId) {
        JobApplication application = getApplicationForUser(id, userId);
        return ApplicationMapper.toResponse(application);
    }

    @Transactional
    public ApplicationResponse createApplication(CreateApplicationRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        JobApplication application = JobApplication.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .role(request.getRole())
                .status("Applied")
                .jobUrl(request.getJobUrl())
                .ctcPackage(request.getCtcPackage())
                .dateApplied(request.getDateApplied() != null ? request.getDateApplied() : LocalDate.now())
                .notes(request.getNotes())
                .build();

        JobApplication saved = applicationRepository.save(application);
        log.info("Application created for user {}: {}", userId, saved.getId());

        return ApplicationMapper.toResponse(saved);
    }

    @Transactional
    public ApplicationResponse updateApplication(UUID id, UpdateApplicationRequest request, UUID userId) {
        JobApplication application = getApplicationForUser(id, userId);

        String previousStatus = application.getStatus();

        application.setCompanyName(request.getCompanyName());
        application.setRole(request.getRole());
        application.setJobUrl(request.getJobUrl());
        application.setCtcPackage(request.getCtcPackage());
        application.setDateApplied(request.getDateApplied());
        application.setNotes(request.getNotes());

        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }

        JobApplication updated = applicationRepository.save(application);

        return ApplicationMapper.toResponse(updated);
    }

    @Transactional
    public ApplicationResponse updateStatus(UUID id, UpdateStatusRequest request, UUID userId) {
        JobApplication application = getApplicationForUser(id, userId);
        String previousStatus = application.getStatus();

        application.setStatus(request.getStatus());
        JobApplication updated = applicationRepository.save(application);

        return ApplicationMapper.toResponse(updated);
    }

    @Transactional
    public void deleteApplication(UUID id, UUID userId) {
        JobApplication application = getApplicationForUser(id, userId);
        String companyName = application.getCompanyName();
        String role = application.getRole();

        applicationRepository.delete(application);

        log.info("Application {} deleted by user {}", id, userId);
    }

    private JobApplication getApplicationForUser(UUID applicationId, UUID userId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));
    }
}
