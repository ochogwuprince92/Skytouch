package com.backend.Skytouch.jobalert.service;

import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.enums.WorkMode;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.mapper.JobAlertMapper;
import com.backend.Skytouch.jobalert.apimodel.JobAlertCreateRequest;
import com.backend.Skytouch.jobalert.entity.JobAlert;
import com.backend.Skytouch.jobalert.repository.JobAlertRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobAlertServiceTest {

    @Mock
    private JobAlertRepository jobAlertRepository;

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobAlertMapper jobAlertMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private JobAlertService jobAlertService;

    @Test
    void create_rejectsEmptyCriteria() {
        assertThatThrownBy(() -> jobAlertService.create("seeker@example.com", new JobAlertCreateRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("criterion");
    }

    @Test
    void create_persistsAlertWithCriteria() {
        UUID seekerId = UUID.randomUUID();
        Users user = Users.builder()
                .id(seekerId)
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .build();
        JobSeeker seeker = JobSeeker.builder().id(seekerId).user(user).build();
        JobAlertCreateRequest request = new JobAlertCreateRequest();
        request.setKeyword("engineer");
        request.setWorkMode(WorkMode.REMOTE);
        JobAlert entity = JobAlert.builder().id(UUID.randomUUID()).active(true).build();

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(seekerId)).thenReturn(Optional.of(seeker));
        when(jobAlertMapper.toEntity(seeker, request)).thenReturn(entity);
        when(jobAlertRepository.save(entity)).thenReturn(entity);
        when(jobAlertMapper.toResponse(entity)).thenReturn(
                com.backend.Skytouch.jobalert.apimodel.JobAlertResponse.builder()
                        .id(entity.getId())
                        .keyword("engineer")
                        .workMode(WorkMode.REMOTE)
                        .active(true)
                        .build());

        jobAlertService.create("seeker@example.com", request);

        verify(jobAlertRepository).save(entity);
    }
}
