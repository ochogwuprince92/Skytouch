package com.backend.Skytouch.company.service;

import com.backend.Skytouch.common.address.AddressValidationService;
import com.backend.Skytouch.common.address.ValidatedAddress;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.CompanyMapper;
import com.backend.Skytouch.company.apimodel.CompanyCreateRequest;
import com.backend.Skytouch.company.apimodel.CompanyResponse;
import com.backend.Skytouch.company.apimodel.CompanyUpdateRequest;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.repository.CompanyRepository;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.employer.repository.EmployerRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private AddressValidationService addressValidationService;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void createForEmployer_linksCompanyAndSyncsName() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("employer@example.com")
                .role(UserRole.EMPLOYER)
                .status(UserStatus.ACTIVE)
                .build();
        Employer employer = Employer.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(UserStatus.ACTIVE)
                .phone("+2348012345678")
                .build();
        CompanyCreateRequest request = new CompanyCreateRequest();
        request.setName("Acme Ltd");
        request.setAddress("12 Allen Avenue, Ikeja, Lagos");

        Company company = Company.builder()
                .id(UUID.randomUUID())
                .name("Acme Ltd")
                .status(CompanyStatus.PENDING)
                .build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(employer));
        when(companyMapper.toEntity(request)).thenReturn(company);
        when(addressValidationService.validate(request.getAddress()))
                .thenReturn(new ValidatedAddress("12 Allen Avenue", "Ikeja", "Lagos"));
        when(companyRepository.save(company)).thenReturn(company);
        when(employerRepository.save(employer)).thenReturn(employer);
        when(companyMapper.toResponse(company)).thenReturn(
                CompanyResponse.builder().id(company.getId()).name("Acme Ltd").build());

        CompanyResponse response = companyService.createForEmployer("employer@example.com", request);

        assertThat(response.getName()).isEqualTo("Acme Ltd");
        assertThat(employer.getCompany()).isSameAs(company);
        assertThat(employer.getCompanyName()).isEqualTo("Acme Ltd");
        assertThat(company.getAddressLine()).isEqualTo("12 Allen Avenue");
        verify(employerRepository).save(employer);
    }

    @Test
    void createForEmployer_throwsWhenAlreadyLinked() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder().id(userId).email("employer@example.com").role(UserRole.EMPLOYER).build();
        Company existing = Company.builder().id(UUID.randomUUID()).name("Existing").status(CompanyStatus.ACTIVE).build();
        Employer employer = Employer.builder().user(user).company(existing).build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(employer));

        CompanyCreateRequest request = new CompanyCreateRequest();
        request.setName("Another Co");

        assertThatThrownBy(() -> companyService.createForEmployer("employer@example.com", request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already has a linked company");
        verify(companyRepository, never()).save(any());
    }

    @Test
    void findMyCompany_throwsWhenNotLinked() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder().id(userId).email("employer@example.com").role(UserRole.EMPLOYER).build();
        Employer employer = Employer.builder().user(user).build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(employer));

        assertThatThrownBy(() -> companyService.findMyCompany("employer@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No company linked");
    }

    @Test
    void updateMyCompany_appliesFieldsAndSyncsEmployerName() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder().id(userId).email("employer@example.com").role(UserRole.EMPLOYER).build();
        Company company = Company.builder()
                .id(UUID.randomUUID())
                .name("Acme Ltd")
                .status(CompanyStatus.PENDING)
                .build();
        Employer employer = Employer.builder().user(user).company(company).companyName("Acme Ltd").build();

        CompanyUpdateRequest request = new CompanyUpdateRequest();
        request.setName("Acme Global");
        request.setDescription("HR tech");

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(employer));
        doAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            CompanyUpdateRequest r = invocation.getArgument(1);
            c.setName(r.getName());
            c.setDescription(r.getDescription());
            return null;
        }).when(companyMapper).applyUpdate(eq(company), eq(request));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toResponse(company)).thenReturn(
                CompanyResponse.builder().name("Acme Global").description("HR tech").build());

        companyService.updateMyCompany("employer@example.com", request);

        verify(companyMapper).applyUpdate(company, request);
        ArgumentCaptor<Employer> employerCaptor = ArgumentCaptor.forClass(Employer.class);
        verify(employerRepository).save(employerCaptor.capture());
        assertThat(employerCaptor.getValue().getCompanyName()).isEqualTo("Acme Global");
    }

    @Test
    void requireLinkedCompany_throwsWhenMissing() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder().id(userId).email("employer@example.com").role(UserRole.EMPLOYER).build();
        Employer employer = Employer.builder().user(user).build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(employer));

        assertThatThrownBy(() -> companyService.requireLinkedCompany("employer@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Create a company profile");
    }
}
