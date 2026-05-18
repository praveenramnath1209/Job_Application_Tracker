package com.jobtracker.controller;

import com.jobtracker.dto.common.ApiResponse;
import com.jobtracker.dto.request.AnalyzeRequest;
import com.jobtracker.dto.response.AnalysisResult;
import com.jobtracker.service.AnalyzerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analyzer")
@RequiredArgsConstructor
public class AnalyzerController {

    private final AnalyzerService analyzerService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AnalysisResult>> analyze(
            @Valid @RequestBody AnalyzeRequest request) {
        AnalysisResult result = analyzerService.analyze(request);
        return ResponseEntity.ok(ApiResponse.success("Analysis complete", result));
    }

    @PostMapping("/analyze-pdf")
    public ResponseEntity<ApiResponse<AnalysisResult>> analyzePdf(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("jobDescription") String jobDescription) {
        
        String resumeText = analyzerService.extractTextFromPdf(file);
        
        AnalyzeRequest request = new AnalyzeRequest();
        request.setJobDescription(jobDescription);
        request.setResumeText(resumeText);
        
        AnalysisResult result = analyzerService.analyze(request);
        return ResponseEntity.ok(ApiResponse.success("PDF Analysis complete", result));
    }
}
