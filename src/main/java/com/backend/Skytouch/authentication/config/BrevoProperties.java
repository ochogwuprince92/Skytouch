package com.backend.Skytouch.authentication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.mail.brevo")
public class BrevoProperties {

    private String apiKey;
    private String apiUrl = "https://api.brevo.com/v3/smtp/email";
    private String senderName = "SkyTouch";
}
