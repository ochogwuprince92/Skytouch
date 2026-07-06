package com.backend.Skytouch.messaging.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.ApplicationMessageMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.messaging.apimodel.ApplicationMessageCreateRequest;
import com.backend.Skytouch.messaging.apimodel.ApplicationMessageResponse;
import com.backend.Skytouch.messaging.entity.ApplicationMessage;
import com.backend.Skytouch.messaging.repository.ApplicationMessageRepository;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationMessageService {

    private final ApplicationMessageRepository messageRepository;
    private final JobApplicationRepository applicationRepository;
    private final ApplicationMessageMapper messageMapper;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<ApplicationMessageResponse> findMessages(String email, UUID applicationId, int page, int size) {
        JobApplication application = getParticipantApplication(email, applicationId);
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.ASC, "sentAt"));
        Page<ApplicationMessage> results = messageRepository.findByApplication_IdOrderBySentAtAsc(
                application.getId(), pageable);
        return PaginationUtils.mapPage(results, messageMapper::toResponse);
    }

    @Transactional
    public ApplicationMessageResponse sendMessage(
            String email, UUID applicationId, ApplicationMessageCreateRequest request) {
        if (!StringUtils.hasText(request.getBody())) {
            throw new BadRequestException("Message body is required");
        }
        JobApplication application = getParticipantApplication(email, applicationId);
        Users sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        ApplicationMessage message = ApplicationMessage.builder()
                .application(application)
                .sender(sender)
                .body(request.getBody().trim())
                .build();

        ApplicationMessage saved = messageRepository.save(message);
        notificationService.notifyOnNewMessage(saved);
        return messageMapper.toResponse(saved);
    }

    @Transactional
    public void markThreadRead(String email, UUID applicationId) {
        JobApplication application = getParticipantApplication(email, applicationId);
        Users reader = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        messageRepository.markThreadReadForRecipient(application.getId(), reader.getId());
    }

    private JobApplication getParticipantApplication(String email, UUID applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (application.getJobSeeker().getUser().getEmail().equals(email)) {
            return application;
        }

        try {
            Company company = companyService.getLinkedCompany(email);
            if (application.getJob().getCompany().getId().equals(company.getId())) {
                return application;
            }
        } catch (BadRequestException | ResourceNotFoundException ignored) {
            // not an employer on this application
        }

        throw new ResourceNotFoundException("Application not found: " + applicationId);
    }
}
