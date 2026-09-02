package com.backend.Skytouch.subscription.apimodel;

import com.backend.Skytouch.subscription.enums.PlanType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeRequest {
    @NotNull(message = "Plan type is required")
    private PlanType plan;
}
