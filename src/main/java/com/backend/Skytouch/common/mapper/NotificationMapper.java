package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.notification.apimodel.NotificationResponse;
import com.backend.Skytouch.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .applicationId(notification.getApplicationId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
