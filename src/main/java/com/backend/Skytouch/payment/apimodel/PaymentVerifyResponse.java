package com.backend.Skytouch.payment.apimodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyResponse {

    private boolean status;
    private String message;
    private PaymentData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentData {
        private String reference;
        private String gatewayResponse;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
        private String channel;
        private String currency;
        private String amount;
        private Map<String, Object> metadata;
        private Customer customer;
        private Authorization authorization;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Customer {
            private String email;
            private String customerCode;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Authorization {
            private String authorizationCode;
            private String bin;
            private String last4;
            private String expMonth;
            private String expYear;
            private String cardType;
            private String bank;
        }
    }
}
