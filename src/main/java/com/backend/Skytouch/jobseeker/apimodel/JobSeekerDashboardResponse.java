package com.backend.Skytouch.jobseeker.apimodel;

import com.backend.Skytouch.common.profile.ProfileCompleteness;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobSeekerDashboardResponse {

    private final String displayName;
    private final boolean emailVerified;
    private final Boolean openToWork;
    private final ProfileCompleteness profileCompleteness;
    private final JobSeekerDashboardStats stats;
}
