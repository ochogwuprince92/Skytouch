package com.backend.Skytouch.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paystack")
@Data
public class PaystackConfig {

    private String secretKey;
    private String publicKey;
    private String callbackUrl;
    private String webhookUrl;
    private String apiUrl;
}
