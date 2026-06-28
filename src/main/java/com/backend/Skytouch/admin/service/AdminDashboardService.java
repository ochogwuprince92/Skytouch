package com.backend.Skytouch.admin.service;

import com.backend.Skytouch.admin.apimodel.AdminDashboardResponse;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .jobSeekers(userRepository.countByRole(UserRole.JOB_SEEKER))
                .employers(userRepository.countByRole(UserRole.EMPLOYER))
                .admins(userRepository.countByRole(UserRole.ADMIN))
                .pendingEmailVerifications(userRepository.countByEmailVerifiedFalse())
                .pendingAccounts(userRepository.countByStatus(UserStatus.PENDING))
                .build();
    }
}
