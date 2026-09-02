package com.backend.Skytouch.jobalert.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobAlertDigestRunResponse {

    private final int seekersNotified;
    private final int jobsIncluded;
}
