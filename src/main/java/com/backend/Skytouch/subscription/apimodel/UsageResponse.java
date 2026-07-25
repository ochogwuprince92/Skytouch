package com.backend.Skytouch.subscription.apimodel;

import com.backend.Skytouch.subscription.enums.PlanType;
import com.backend.Skytouch.subscription.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageResponse {
    private PlanType plan;
    private SubscriptionStatus status;
    private LocalDateTime expiresAt;
    private int activeJobs;
    private Integer remainingSlots;
    private boolean unlimited;
    private boolean canPublish;
}
