package com.jobtracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {

    private long totalApplications;
    private long activeApplications;
    private long offersReceived;
    private long interviewsScheduled;
    private double offerConversionRate;
    private double interviewSuccessRate;
    private Map<String, Long> statusDistribution;
    private List<MonthlyCount> monthlyApplications;
    private List<CTCRange> ctcDistribution;

    @Data
    @Builder
    public static class MonthlyCount {
        private String month;
        private long count;
    }

    @Data
    @Builder
    public static class CTCRange {
        private String range;
        private long count;
    }
}
