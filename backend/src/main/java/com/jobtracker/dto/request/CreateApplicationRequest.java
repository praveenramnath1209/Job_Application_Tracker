package com.jobtracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateApplicationRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name must not exceed 150 characters")
    private String companyName;

    @NotBlank(message = "Role is required")
    @Size(max = 150, message = "Role must not exceed 150 characters")
    private String role;

    @Size(max = 500, message = "Job URL must not exceed 500 characters")
    private String jobUrl;

    private BigDecimal ctcPackage;

    private LocalDate dateApplied;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;
}
