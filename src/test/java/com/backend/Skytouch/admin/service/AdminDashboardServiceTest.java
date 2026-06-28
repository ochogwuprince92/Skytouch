package com.backend.Skytouch.admin.service;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getDashboard_aggregatesUserCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole(UserRole.JOB_SEEKER)).thenReturn(7L);
        when(userRepository.countByRole(UserRole.EMPLOYER)).thenReturn(2L);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);
        when(userRepository.countByEmailVerifiedFalse()).thenReturn(3L);
        when(userRepository.countByStatus(UserStatus.PENDING)).thenReturn(4L);

        var result = adminDashboardService.getDashboard();

        assertThat(result.getTotalUsers()).isEqualTo(10);
        assertThat(result.getJobSeekers()).isEqualTo(7);
        assertThat(result.getEmployers()).isEqualTo(2);
        assertThat(result.getAdmins()).isEqualTo(1);
        assertThat(result.getPendingEmailVerifications()).isEqualTo(3);
        assertThat(result.getPendingAccounts()).isEqualTo(4);
    }
}
