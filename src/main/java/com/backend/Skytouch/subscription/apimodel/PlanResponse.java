package com.backend.Skytouch.subscription.apimodel;

import com.backend.Skytouch.subscription.enums.BillingCycle;
import com.backend.Skytouch.subscription.enums.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private PlanType plan;
    private String name;
    private String description;
    private BigDecimal price;
    private BillingCycle billingCycle;
    private int maxJobSlots;
    private boolean unlimited;
}
