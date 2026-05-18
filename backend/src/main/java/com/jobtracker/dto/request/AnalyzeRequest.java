package com.jobtracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnalyzeRequest {

    @NotBlank(message = "Job description is required")
    @Size(min = 50, max = 10000, message = "Job description must be between 50 and 10000 characters")
    private String jobDescription;

    @NotBlank(message = "Resume/profile text is required")
    @Size(min = 50, max = 10000, message = "Resume text must be between 50 and 10000 characters")
    private String resumeText;
}
