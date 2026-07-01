package com.backend.Skytouch.notification.apimodel;

import com.backend.Skytouch.common.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationResponse {

    private final UUID id;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final UUID applicationId;
    private final boolean read;
    private final LocalDateTime createdAt;
}
