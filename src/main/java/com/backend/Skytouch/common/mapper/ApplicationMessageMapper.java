package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.messaging.apimodel.ApplicationMessageResponse;
import com.backend.Skytouch.messaging.entity.ApplicationMessage;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMessageMapper {

    public ApplicationMessageResponse toResponse(ApplicationMessage message) {
        return ApplicationMessageResponse.builder()
                .id(message.getId())
                .applicationId(message.getApplication().getId())
                .senderEmail(message.getSender().getEmail())
                .senderRole(message.getSender().getRole())
                .body(message.getBody())
                .sentAt(message.getSentAt())
                .read(message.getReadAt() != null)
                .build();
    }
}
