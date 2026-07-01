package com.backend.Skytouch.jobalert.scheduler;

import com.backend.Skytouch.jobalert.config.JobAlertDigestProperties;
import com.backend.Skytouch.jobalert.service.JobAlertDigestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobAlertDigestScheduler {

    private final JobAlertDigestService digestService;
    private final JobAlertDigestProperties properties;

    @Scheduled(cron = "${app.job-alert.digest.cron:0 0 8 * * *}")
    public void runScheduledDigest() {
        if (!properties.isEnabled()) {
            return;
        }
        log.info("Running scheduled job alert digest");
        digestService.runDigest();
    }
}
