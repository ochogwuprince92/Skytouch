package com.backend.Skytouch.analytics.util;

import com.backend.Skytouch.common.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FunnelCountBuilderTest {

    @Test
    void fromGroupedResults_mapsStatusCounts() {
        List<Object[]> rows = List.of(
                new Object[]{ApplicationStatus.SUBMITTED, 5L},
                new Object[]{ApplicationStatus.HIRED, 2L},
                new Object[]{ApplicationStatus.WITHDRAWN, 1L});

        var funnel = FunnelCountBuilder.fromGroupedResults(rows);

        assertThat(funnel.getSubmitted()).isEqualTo(5);
        assertThat(funnel.getHired()).isEqualTo(2);
        assertThat(funnel.getWithdrawn()).isEqualTo(1);
        assertThat(funnel.getTotal()).isEqualTo(8);
    }

    @Test
    void hireRatePercent_excludesWithdrawn() {
        var funnel = com.backend.Skytouch.analytics.apimodel.ApplicationFunnelCounts.builder()
                .submitted(4)
                .hired(1)
                .withdrawn(2)
                .total(7)
                .reviewing(0)
                .shortlisted(0)
                .interviewScheduled(0)
                .offerExtended(0)
                .offerDeclined(0)
                .rejected(0)
                .build();

        assertThat(FunnelCountBuilder.hireRatePercent(funnel)).isEqualTo(20.0);
    }
}
