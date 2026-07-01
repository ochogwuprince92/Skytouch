package com.backend.Skytouch.offer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.offer.expiry")
public class OfferExpiryProperties {

    private boolean enabled = true;
    private String cron = "0 0 * * * *";
}
