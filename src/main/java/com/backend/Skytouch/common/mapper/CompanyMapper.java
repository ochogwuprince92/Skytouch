package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.company.apimodel.CompanyCreateRequest;
import com.backend.Skytouch.company.apimodel.CompanyResponse;
import com.backend.Skytouch.company.apimodel.CompanyUpdateRequest;
import com.backend.Skytouch.company.entity.Company;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CompanyMapper {

    public Company toEntity(CompanyCreateRequest request) {
        return Company.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .industry(request.getIndustry())
                .website(request.getWebsite())
                .status(CompanyStatus.PENDING)
                .build();
    }

    public void applyUpdate(Company company, CompanyUpdateRequest request) {
        if (StringUtils.hasText(request.getName())) {
            company.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }
        if (request.getIndustry() != null) {
            company.setIndustry(request.getIndustry());
        }
        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }
    }

    public CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .industry(company.getIndustry())
                .website(company.getWebsite())
                .logoUrl(company.getLogoUrl())
                .addressLine(company.getAddressLine())
                .addressLga(company.getAddressLga())
                .addressState(company.getAddressState())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
