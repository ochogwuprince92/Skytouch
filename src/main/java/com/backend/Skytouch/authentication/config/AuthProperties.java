package com.backend.Skytouch.authentication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private Otp otp = new Otp();
    private Session session = new Session();
    private String emailFrom = "noreply@skytouch.com";
    private boolean logOtp = false;
    private boolean mailSendEnabled = true;

    @Getter
    @Setter
    public static class Otp {
        private int length = 6;
        private long expirationMs = 600_000L;
        private int maxAttempts = 5;
    }

    @Getter
    @Setter
    public static class Session {
        private long expirationMs = 86_400_000L;
    }
}
