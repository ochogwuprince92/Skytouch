package com.backend.Skytouch.employer.apimodel;

import com.backend.Skytouch.common.profile.ProfileCompleteness;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployerDashboardResponse {

    private final String displayName;
    private final String companyName;
    private final boolean emailVerified;
    private final boolean companyLinked;
    private final ProfileCompleteness profileCompleteness;
    private final EmployerDashboardStats stats;
}
