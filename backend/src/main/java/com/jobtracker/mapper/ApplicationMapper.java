package com.jobtracker.mapper;

import com.jobtracker.dto.response.ApplicationResponse;
import com.jobtracker.entity.JobApplication;

public final class ApplicationMapper {

    private ApplicationMapper() {}

    public static ApplicationResponse toResponse(JobApplication app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .companyName(app.getCompanyName())
                .role(app.getRole())
                .status(app.getStatus())
                .jobUrl(app.getJobUrl())
                .ctcPackage(app.getCtcPackage())
                .dateApplied(app.getDateApplied())
                .notes(app.getNotes())
                .createdAt(app.getCreatedAt())
                .build();
    }
}
