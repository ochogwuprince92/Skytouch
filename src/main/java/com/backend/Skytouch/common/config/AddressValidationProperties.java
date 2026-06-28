package com.backend.Skytouch.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "address.validation")
public class AddressValidationProperties {

    private boolean enabled = false;
    private String apiKey;
    private String endpoint = "https://maps.googleapis.com/maps/api/geocode/json";
}
