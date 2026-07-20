package com.backend.Skytouch.payment.service;

import com.backend.Skytouch.common.config.PaystackConfig;
import com.backend.Skytouch.payment.apimodel.PaymentInitializeRequest;
import com.backend.Skytouch.payment.apimodel.PaymentInitializeResponse;
import com.backend.Skytouch.payment.apimodel.PaymentVerifyResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaystackConfig paystackConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentInitializeResponse initializePayment(PaymentInitializeRequest request) {
        try {
            String reference = request.getReference() != null ? request.getReference() : generateReference();
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", request.getEmail());
            requestBody.put("amount", request.getAmount().multiply(new BigDecimal("100")).intValue()); // Convert to kobo
            requestBody.put("currency", request.getCurrency());
            requestBody.put("reference", reference);
            requestBody.put("callback_url", request.getCallbackUrl());
            if (request.getMetadata() != null) {
                requestBody.put("metadata", request.getMetadata());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(paystackConfig.getSecretKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = paystackConfig.getApiUrl() + "/transaction/initialize";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            boolean status = rootNode.path("status").asBoolean();
            String message = rootNode.path("message").asText();
            
            if (status && rootNode.has("data")) {
                JsonNode dataNode = rootNode.path("data");
                PaymentInitializeResponse.PaymentData data = PaymentInitializeResponse.PaymentData.builder()
                        .authorizationUrl(dataNode.path("authorization_url").asText())
                        .accessCode(dataNode.path("access_code").asText())
                        .reference(dataNode.path("reference").asText())
                        .build();
                
                return PaymentInitializeResponse.builder()
                        .status(true)
                        .message(message)
                        .data(data)
                        .build();
            } else {
                return PaymentInitializeResponse.builder()
                        .status(false)
                        .message(message)
                        .data(null)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error initializing payment", e);
            return PaymentInitializeResponse.builder()
                    .status(false)
                    .message("Payment initialization failed: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    public PaymentVerifyResponse verifyPayment(String reference) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(paystackConfig.getSecretKey());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = paystackConfig.getApiUrl() + "/transaction/verify/" + reference;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            boolean status = rootNode.path("status").asBoolean();
            String message = rootNode.path("message").asText();
            
            if (status && rootNode.has("data")) {
                JsonNode dataNode = rootNode.path("data");
                
                PaymentVerifyResponse.PaymentData.Customer customer = null;
                if (dataNode.has("customer")) {
                    JsonNode customerNode = dataNode.path("customer");
                    customer = PaymentVerifyResponse.PaymentData.Customer.builder()
                            .email(customerNode.path("email").asText())
                            .customerCode(customerNode.path("customer_code").asText())
                            .build();
                }

                PaymentVerifyResponse.PaymentData.Authorization authorization = null;
                if (dataNode.has("authorization")) {
                    JsonNode authNode = dataNode.path("authorization");
                    authorization = PaymentVerifyResponse.PaymentData.Authorization.builder()
                            .authorizationCode(authNode.path("authorization_code").asText())
                            .bin(authNode.path("bin").asText())
                            .last4(authNode.path("last4").asText())
                            .expMonth(authNode.path("exp_month").asText())
                            .expYear(authNode.path("exp_year").asText())
                            .cardType(authNode.path("card_type").asText())
                            .bank(authNode.path("bank").asText())
                            .build();
                }

                Map<String, Object> metadata = new HashMap<>();
                if (dataNode.has("metadata")) {
                    JsonNode metadataNode = dataNode.path("metadata");
                    metadataNode.fields().forEachRemaining(entry ->
                        metadata.put(entry.getKey(), entry.getValue().asText())
                    );
                }

                PaymentVerifyResponse.PaymentData data = PaymentVerifyResponse.PaymentData.builder()
                        .reference(dataNode.path("reference").asText())
                        .gatewayResponse(dataNode.path("gateway_response").asText())
                        .paidAt(parseDateTime(dataNode.path("paid_at").asText()))
                        .createdAt(parseDateTime(dataNode.path("createdAt").asText()))
                        .channel(dataNode.path("channel").asText())
                        .currency(dataNode.path("currency").asText())
                        .amount(dataNode.path("amount").asText())
                        .metadata(metadata)
                        .customer(customer)
                        .authorization(authorization)
                        .build();
                
                return PaymentVerifyResponse.builder()
                        .status(true)
                        .message(message)
                        .data(data)
                        .build();
            } else {
                return PaymentVerifyResponse.builder()
                        .status(false)
                        .message(message)
                        .data(null)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return PaymentVerifyResponse.builder()
                    .status(false)
                    .message("Payment verification failed: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    private String generateReference() {
        return "SKY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private java.time.LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDateTime.parse(dateTime, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}", dateTime);
            return null;
        }
    }
}
