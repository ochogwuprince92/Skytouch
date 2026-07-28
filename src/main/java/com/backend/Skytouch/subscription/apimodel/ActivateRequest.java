package com.backend.Skytouch.subscription.apimodel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateRequest {
    @NotBlank(message = "Payment reference is required")
    private String paymentReference;
}
