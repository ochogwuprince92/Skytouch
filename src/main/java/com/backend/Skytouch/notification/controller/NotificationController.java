package com.backend.Skytouch.notification.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.notification.apimodel.NotificationResponse;
import com.backend.Skytouch.notification.apimodel.UnreadCountResponse;
import com.backend.Skytouch.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER')")
    public PageResponse<NotificationResponse> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.findMyNotifications(SecurityUtils.getCurrentUser().getEmail(), page, size);
    }

    @GetMapping("/me/unread-count")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER')")
    public UnreadCountResponse getUnreadCount() {
        return notificationService.countUnread(SecurityUtils.getCurrentUser().getEmail());
    }

    @PatchMapping("/me/{id}/read")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER')")
    public NotificationResponse markAsRead(@PathVariable UUID id) {
        return notificationService.markAsRead(SecurityUtils.getCurrentUser().getEmail(), id);
    }

    @PostMapping("/me/read-all")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER')")
    public void markAllAsRead() {
        notificationService.markAllAsRead(SecurityUtils.getCurrentUser().getEmail());
    }
}
