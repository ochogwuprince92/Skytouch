package com.backend.Skytouch.admin.bootstrap;

import com.backend.Skytouch.admin.config.AdminBootstrapProperties;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (userRepository.countByRole(UserRole.ADMIN) > 0) {
            return;
        }
        if (!StringUtils.hasText(properties.getEmail()) || !StringUtils.hasText(properties.getPassword())) {
            log.warn("Admin bootstrap enabled but app.admin.bootstrap.email/password not set — skipping");
            return;
        }
        if (properties.getPassword().length() < 8) {
            log.warn("Admin bootstrap password must be at least 8 characters — skipping");
            return;
        }
        if (userRepository.existsByEmail(properties.getEmail())) {
            log.warn("Admin bootstrap email already registered — skipping admin seed");
            return;
        }

        Users admin = Users.builder()
                .email(properties.getEmail())
                .password(passwordEncoder.encode(properties.getPassword()))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .active(true)
                .build();
        userRepository.save(admin);
        log.info("Bootstrapped admin user: {}", properties.getEmail());
    }
}
