package com.backend.Skytouch.admin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.admin.bootstrap")
public class AdminBootstrapProperties {

    private boolean enabled = false;
    private String email;
    private String password;
}
