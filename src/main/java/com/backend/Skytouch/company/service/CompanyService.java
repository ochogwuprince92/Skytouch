package com.backend.Skytouch.company.service;

import com.backend.Skytouch.common.address.AddressValidationService;
import com.backend.Skytouch.common.address.ValidatedAddress;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.UserRole;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private static final UserRole EMPLOYER_ROLE = UserRole.EMPLOYER;

    private final CompanyRepository companyRepository;
    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;
    private final AddressValidationService addressValidationService;

    @Transactional
    public CompanyResponse createForEmployer(String email, CompanyCreateRequest request) {
        Employer employer = getEmployerProfile(email);
        if (employer.getCompany() != null) {
            throw new ConflictException("Employer already has a linked company");
        }

        Company company = companyMapper.toEntity(request);
        applyValidatedAddress(company, request.getAddress());
        Company savedCompany = companyRepository.save(company);

        employer.setCompany(savedCompany);
        employer.setCompanyName(savedCompany.getName());
        employerRepository.save(employer);

        // Force immediate flush to ensure transaction is committed before returning
        employerRepository.flush();

        return companyMapper.toResponse(savedCompany);
    }

    @Transactional
    public Company createForAdmin(CompanyCreateRequest request) {
        Company company = companyMapper.toEntity(request);
        company.setStatus(CompanyStatus.ACTIVE); // Admin-created companies are auto-approved
        applyValidatedAddress(company, request.getAddress());
        return companyRepository.save(company);
    }

    @Transactional(readOnly = true)
    public CompanyResponse findMyCompany(String email) {
        Employer employer = getEmployerProfile(email);
        Company company = employer.getCompany();
        if (company == null) {
            throw new ResourceNotFoundException("No company linked to this employer");
        }
        return companyMapper.toResponse(company);
    }

    @Transactional
    public CompanyResponse updateMyCompany(String email, CompanyUpdateRequest request) {
        Employer employer = getEmployerProfile(email);
        Company company = employer.getCompany();
        if (company == null) {
            throw new ResourceNotFoundException("No company linked to this employer");
        }

        companyMapper.applyUpdate(company, request);
        if (StringUtils.hasText(request.getAddress())) {
            applyValidatedAddress(company, request.getAddress());
        }
        if (StringUtils.hasText(company.getName())) {
            employer.setCompanyName(company.getName());
            employerRepository.save(employer);
        }

        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        return companyMapper.toResponse(company);
    }

    @Transactional(readOnly = true)
    public void requireLinkedCompany(String email) {
        getLinkedCompany(email);
    }

    @Transactional(readOnly = true)
    public Company getLinkedCompany(String email) {
        Employer employer = getEmployerProfile(email);
        Company company = employer.getCompany();
        if (company == null) {
            throw new BadRequestException("Create a company profile before posting jobs");
        }
        return company;
    }

    @Transactional(readOnly = true)
    public void requireActiveCompany(String email) {
        Company company = getLinkedCompany(email);
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BadRequestException("Company must be approved by an admin before publishing jobs");
        }
    }

    private Employer getEmployerProfile(String email) {
        Users user = userRepository.findByEmailAndRole(email, EMPLOYER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + email));
        return employerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employer profile not found: " + email));
    }

    private void applyValidatedAddress(Company company, String address) {
        ValidatedAddress validated = addressValidationService.validate(address);
        if (validated == null) {
            return;
        }
        company.setAddressLine(validated.addressLine());
        if (StringUtils.hasText(validated.lga())) {
            company.setAddressLga(validated.lga());
        }
        if (StringUtils.hasText(validated.state())) {
            company.setAddressState(validated.state());
        }
    }
}
