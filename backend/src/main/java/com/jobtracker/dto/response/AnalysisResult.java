package com.jobtracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalysisResult {

    private double matchPercentage;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> recommendations;
    private int totalJobKeywords;
    private int matchedKeywords;
    private String overallFeedback;
}
