package com.backend.Skytouch.jobalert.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(JobAlertDigestProperties.class)
public class JobAlertConfig {
}
