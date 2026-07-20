package com.backend.Skytouch.payment.apimodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitializeResponse {

    private boolean status;
    private String message;
    private PaymentData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentData {
        private String authorizationUrl;
        private String accessCode;
        private String reference;
    }
}
