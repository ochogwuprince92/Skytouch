package com.backend.Skytouch.subscription.apimodel;

import com.backend.Skytouch.subscription.enums.BillingCycle;
import com.backend.Skytouch.subscription.enums.PlanType;
import com.backend.Skytouch.subscription.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private UUID companyId;
    private PlanType plan;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime expiresAt;
    private BillingCycle billingCycle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
