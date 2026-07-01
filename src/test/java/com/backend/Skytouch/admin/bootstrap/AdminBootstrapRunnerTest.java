package com.backend.Skytouch.admin.bootstrap;

import com.backend.Skytouch.admin.config.AdminBootstrapProperties;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock private AdminBootstrapProperties properties;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminBootstrapRunner runner;

    @Test
    void run_createsAdminWhenEnabledAndNoneExists() throws Exception {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getEmail()).thenReturn("admin@example.com");
        when(properties.getPassword()).thenReturn("SecurePass123!");
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(0L);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed");
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        runner.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("admin@example.com");
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(captor.getValue().getEmailVerified()).isTrue();
    }

    @Test
    void run_skipsWhenAdminAlreadyExists() throws Exception {
        when(properties.isEnabled()).thenReturn(true);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository, never()).save(any());
    }
}
