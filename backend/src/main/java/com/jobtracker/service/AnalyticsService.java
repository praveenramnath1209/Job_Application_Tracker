package com.jobtracker.service;

import com.jobtracker.dto.response.AnalyticsResponse;
import com.jobtracker.dto.response.AnalyticsResponse.CTCRange;
import com.jobtracker.dto.response.AnalyticsResponse.MonthlyCount;
import com.jobtracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final JobApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse getSummary(UUID userId) {
        long total = applicationRepository.countByUserId(userId);
        long offers = applicationRepository.countByUserIdAndStatus(userId, "Offer Received");
        long rejected = applicationRepository.countByUserIdAndStatus(userId, "Rejected");
        long active = total - offers - rejected;

        long upcomingInterviews = 0; // Derived from custom statuses if needed in future

        double offerConversionRate = total > 0 ? Math.round((offers * 100.0 / total) * 10.0) / 10.0 : 0.0;

        // Since statuses are custom, estimating interview success rate without exact "Tech Interview" round counts:
        long interviewTotal = upcomingInterviews + offers + rejected; // Approximation
        double interviewSuccessRate = interviewTotal > 0
                ? Math.round((offers * 100.0 / interviewTotal) * 10.0) / 10.0
                : 0.0;

        List<Object[]> statusCounts = applicationRepository.countByStatusForUser(userId);
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        for (Object[] row : statusCounts) {
            statusDistribution.put((String) row[0], (Long) row[1]);
        }

        List<Object[]> monthly = applicationRepository.countMonthlyApplicationsForUser(
                userId, LocalDateTime.now().minusDays(180));
        List<MonthlyCount> monthlyCounts = monthly.stream()
                .map(row -> MonthlyCount.builder().month((String) row[0]).count(((Number) row[1]).longValue()).build())
                .collect(Collectors.toList());

        List<BigDecimal> ctcList = applicationRepository.findCtcPackagesByUserId(userId);

        return AnalyticsResponse.builder()
                .totalApplications(total)
                .activeApplications(active)
                .offersReceived(offers)
                .interviewsScheduled(upcomingInterviews)
                .offerConversionRate(offerConversionRate)
                .interviewSuccessRate(interviewSuccessRate)
                .statusDistribution(statusDistribution)
                .monthlyApplications(monthlyCounts)
                .ctcDistribution(buildCTCDistribution(ctcList))
                .build();
    }

    private List<CTCRange> buildCTCDistribution(List<BigDecimal> ctcValues) {
        Map<String, Long> ranges = new LinkedHashMap<>();
        ranges.put("< 5 LPA", 0L);
        ranges.put("5-10 LPA", 0L);
        ranges.put("10-20 LPA", 0L);
        ranges.put("20-30 LPA", 0L);
        ranges.put("30-50 LPA", 0L);
        ranges.put("> 50 LPA", 0L);
        for (BigDecimal ctc : ctcValues) {
            double v = ctc.doubleValue();
            if (v < 5) ranges.merge("< 5 LPA", 1L, Long::sum);
            else if (v < 10) ranges.merge("5-10 LPA", 1L, Long::sum);
            else if (v < 20) ranges.merge("10-20 LPA", 1L, Long::sum);
            else if (v < 30) ranges.merge("20-30 LPA", 1L, Long::sum);
            else if (v < 50) ranges.merge("30-50 LPA", 1L, Long::sum);
            else ranges.merge("> 50 LPA", 1L, Long::sum);
        }
        return ranges.entrySet().stream()
                .map(e -> CTCRange.builder().range(e.getKey()).count(e.getValue()).build())
                .collect(Collectors.toList());
    }
}
