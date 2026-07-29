package com.backend.Skytouch.admin.apimodel;

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
public class SubscriptionModerationResponse {
    private UUID id;
    private String companyName;
    private String companyEmail;
    private PlanType plan;
    private SubscriptionStatus status;
    private BillingCycle billingCycle;
    private LocalDateTime startDate;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
