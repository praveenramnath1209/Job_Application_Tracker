package com.jobtracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {

    private UUID id;
    private String companyName;
    private String role;
    private String status;
    private String jobUrl;
    private BigDecimal ctcPackage;
    private LocalDate dateApplied;
    private String notes;
    private LocalDateTime createdAt;
}
