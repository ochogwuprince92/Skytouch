package com.backend.Skytouch.messaging.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.messaging.apimodel.ApplicationMessageCreateRequest;
import com.backend.Skytouch.messaging.apimodel.ApplicationMessageResponse;
import com.backend.Skytouch.messaging.service.ApplicationMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications/{applicationId}/messages")
@RequiredArgsConstructor
public class ApplicationMessageController {

    private final ApplicationMessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'JOB_SEEKER')")
    public PageResponse<ApplicationMessageResponse> getMessages(
            @PathVariable UUID applicationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return messageService.findMessages(
                SecurityUtils.getCurrentUser().getEmail(), applicationId, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EMPLOYER', 'JOB_SEEKER')")
    public ApplicationMessageResponse sendMessage(
            @PathVariable UUID applicationId,
            @Valid @RequestBody ApplicationMessageCreateRequest request) {
        return messageService.sendMessage(
                SecurityUtils.getCurrentUser().getEmail(), applicationId, request);
    }

    @PostMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('EMPLOYER', 'JOB_SEEKER')")
    public void markRead(@PathVariable UUID applicationId) {
        messageService.markThreadRead(SecurityUtils.getCurrentUser().getEmail(), applicationId);
    }
}
