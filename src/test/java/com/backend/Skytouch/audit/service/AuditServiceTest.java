package com.backend.Skytouch.audit.service;

import com.backend.Skytouch.audit.entity.AuditEvent;
import com.backend.Skytouch.audit.repository.AuditEventRepository;
import com.backend.Skytouch.common.enums.AuditAction;
import com.backend.Skytouch.common.enums.AuditTargetType;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.mapper.AuditMapper;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditMapper auditMapper;

    @InjectMocks
    private AuditService auditService;

    @Test
    void record_persistsAuditEvent() {
        UUID adminId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Users admin = Users.builder().id(adminId).email("admin@example.com").role(UserRole.ADMIN).build();

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(auditEventRepository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.record(adminId, AuditAction.COMPANY_APPROVED, AuditTargetType.COMPANY,
                companyId, "Approved company: Acme Ltd");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.COMPANY_APPROVED);
        assertThat(captor.getValue().getTargetId()).isEqualTo(companyId);
    }
}
