package com.backend.Skytouch.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private boolean cloudinaryEnabled = true;
    private String localUploadDir = "uploads";
}
