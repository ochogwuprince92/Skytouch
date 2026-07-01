package com.backend.Skytouch.notification.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponse {

    private final long unreadCount;
}
