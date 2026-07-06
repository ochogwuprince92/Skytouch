package com.backend.Skytouch.jobalert.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.job-alert.digest")
public class JobAlertDigestProperties {

    private boolean enabled = true;
    private String cron = "0 0 8 * * *";
    private int lookbackHours = 24;
}
