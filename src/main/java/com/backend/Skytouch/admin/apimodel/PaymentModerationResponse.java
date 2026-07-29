package com.backend.Skytouch.admin.apimodel;

import com.backend.Skytouch.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentModerationResponse {
    private UUID id;
    private String reference;
    private String companyName;
    private String customerEmail;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String gatewayResponse;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
