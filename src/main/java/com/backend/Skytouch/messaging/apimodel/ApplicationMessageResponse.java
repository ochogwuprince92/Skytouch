package com.backend.Skytouch.messaging.apimodel;

import com.backend.Skytouch.common.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ApplicationMessageResponse {

    private final UUID id;
    private final UUID applicationId;
    private final String senderEmail;
    private final UserRole senderRole;
    private final String body;
    private final LocalDateTime sentAt;
    private final boolean read;
}
