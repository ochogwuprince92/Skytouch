package com.backend.Skytouch.analytics.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationFunnelCounts {

    private final long submitted;
    private final long reviewing;
    private final long shortlisted;
    private final long interviewScheduled;
    private final long offerExtended;
    private final long offerDeclined;
    private final long hired;
    private final long rejected;
    private final long withdrawn;
    private final long total;
}
