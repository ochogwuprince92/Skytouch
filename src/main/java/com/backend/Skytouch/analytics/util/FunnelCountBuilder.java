package com.backend.Skytouch.analytics.util;

import com.backend.Skytouch.analytics.apimodel.ApplicationFunnelCounts;
import com.backend.Skytouch.common.enums.ApplicationStatus;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class FunnelCountBuilder {

    private FunnelCountBuilder() {
    }

    public static ApplicationFunnelCounts fromGroupedResults(List<Object[]> rows) {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (Object[] row : rows) {
            counts.put((ApplicationStatus) row[0], (Long) row[1]);
        }
        long submitted = counts.getOrDefault(ApplicationStatus.SUBMITTED, 0L);
        long reviewing = counts.getOrDefault(ApplicationStatus.REVIEWING, 0L);
        long shortlisted = counts.getOrDefault(ApplicationStatus.SHORTLISTED, 0L);
        long interviewScheduled = counts.getOrDefault(ApplicationStatus.INTERVIEW_SCHEDULED, 0L);
        long offerExtended = counts.getOrDefault(ApplicationStatus.OFFER_EXTENDED, 0L);
        long offerDeclined = counts.getOrDefault(ApplicationStatus.OFFER_DECLINED, 0L);
        long hired = counts.getOrDefault(ApplicationStatus.HIRED, 0L);
        long rejected = counts.getOrDefault(ApplicationStatus.REJECTED, 0L);
        long withdrawn = counts.getOrDefault(ApplicationStatus.WITHDRAWN, 0L);
        long total = submitted + reviewing + shortlisted + interviewScheduled
                + offerExtended + offerDeclined + hired + rejected + withdrawn;

        return ApplicationFunnelCounts.builder()
                .submitted(submitted)
                .reviewing(reviewing)
                .shortlisted(shortlisted)
                .interviewScheduled(interviewScheduled)
                .offerExtended(offerExtended)
                .offerDeclined(offerDeclined)
                .hired(hired)
                .rejected(rejected)
                .withdrawn(withdrawn)
                .total(total)
                .build();
    }

    public static ApplicationFunnelCounts empty() {
        return ApplicationFunnelCounts.builder()
                .submitted(0)
                .reviewing(0)
                .shortlisted(0)
                .interviewScheduled(0)
                .offerExtended(0)
                .offerDeclined(0)
                .hired(0)
                .rejected(0)
                .withdrawn(0)
                .total(0)
                .build();
    }

    public static double hireRatePercent(ApplicationFunnelCounts funnel) {
        long eligible = funnel.getTotal() - funnel.getWithdrawn();
        if (eligible <= 0) {
            return 0.0;
        }
        return roundPercent((double) funnel.getHired() / eligible * 100.0);
    }

    public static double shortlistToHireRatePercent(ApplicationFunnelCounts funnel) {
        long shortlistedPipeline = funnel.getShortlisted()
                + funnel.getInterviewScheduled()
                + funnel.getOfferExtended()
                + funnel.getOfferDeclined()
                + funnel.getHired();
        if (shortlistedPipeline <= 0) {
            return 0.0;
        }
        return roundPercent((double) funnel.getHired() / shortlistedPipeline * 100.0);
    }

    private static double roundPercent(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
