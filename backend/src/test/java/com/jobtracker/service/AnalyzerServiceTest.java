package com.jobtracker.service;

import com.jobtracker.dto.request.AnalyzeRequest;
import com.jobtracker.dto.response.AnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerServiceTest {

    private AnalyzerService analyzerService;

    @BeforeEach
    void setUp() {
        analyzerService = new AnalyzerService();
    }

    @Test
    @DisplayName("Should return high match percentage when resume matches JD skills")
    void shouldReturnHighMatchForMatchingResume() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setJobDescription(
                "We are looking for a Java Spring Boot developer with experience in PostgreSQL, " +
                "Docker, Kubernetes, REST API design, and microservices architecture. " +
                "AWS experience is a plus. Strong understanding of Spring Security and JWT authentication required."
        );
        request.setResumeText(
                "3 years of experience with Java, Spring Boot, Spring Security JWT based authentication. " +
                "Worked with PostgreSQL databases, Docker containers, and REST API development. " +
                "Experience with microservices and AWS EC2. Built Kubernetes deployments."
        );

        AnalysisResult result = analyzerService.analyze(request);

        assertThat(result.getMatchPercentage()).isGreaterThan(60.0);
        assertThat(result.getMatchedSkills()).isNotEmpty();
        assertThat(result.getRecommendations()).isNotEmpty();
        assertThat(result.getOverallFeedback()).isNotNull();
    }

    @Test
    @DisplayName("Should return low match percentage for unrelated resume")
    void shouldReturnLowMatchForUnrelatedResume() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setJobDescription(
                "Senior Java developer with Spring Boot, PostgreSQL, Docker, Kubernetes, " +
                "microservices, Kafka, Redis, AWS, CI/CD, Jenkins experience required."
        );
        request.setResumeText(
                "Graphic designer with 5 years experience in Adobe Photoshop, Illustrator, " +
                "UI/UX design, Figma, logo creation, print media, brand identity, and color theory."
        );

        AnalysisResult result = analyzerService.analyze(request);

        assertThat(result.getMatchPercentage()).isLessThan(30.0);
        assertThat(result.getMissingSkills()).isNotEmpty();
    }

    @Test
    @DisplayName("Should detect semantic aliases — ReactJS should match React")
    void shouldDetectSemanticAliases() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setJobDescription(
                "We need a React.js developer with experience in TypeScript and REST APIs. " +
                "ReactJS knowledge is required for building modern frontend applications."
        );
        request.setResumeText(
                "Built multiple applications using React and TypeScript. " +
                "Experienced in REST API integration and modern JavaScript frameworks."
        );

        AnalysisResult result = analyzerService.analyze(request);

        assertThat(result.getMatchPercentage()).isGreaterThan(50.0);
        assertThat(result.getMatchedSkills()).anyMatch(s -> s.toLowerCase().contains("react"));
    }

    @Test
    @DisplayName("Should always return non-null recommendations")
    void shouldAlwaysReturnRecommendations() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setJobDescription("Python machine learning engineer with TensorFlow and data science experience. Proficient in pandas and numpy for data analysis.");
        request.setResumeText("Recent graduate with Python basics. Completed some online courses on data analysis and pandas. Familiar with basic machine learning concepts.");

        AnalysisResult result = analyzerService.analyze(request);

        assertThat(result).isNotNull();
        assertThat(result.getRecommendations()).isNotNull().isNotEmpty();
        assertThat(result.getTotalJobKeywords()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Match percentage should be between 0 and 100")
    void matchPercentageShouldBeBounded() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setJobDescription("Java Spring Boot PostgreSQL Docker Kubernetes AWS microservices REST API Git Linux");
        request.setResumeText("Java Spring Boot PostgreSQL Docker Kubernetes AWS microservices REST API Git Linux expert with 10 years experience");

        AnalysisResult result = analyzerService.analyze(request);

        assertThat(result.getMatchPercentage()).isBetween(0.0, 100.0);
    }
}
