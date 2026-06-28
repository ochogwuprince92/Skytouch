package com.backend.Skytouch.admin.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {

    private final long totalUsers;
    private final long jobSeekers;
    private final long employers;
    private final long admins;
    private final long pendingEmailVerifications;
    private final long pendingAccounts;
}
