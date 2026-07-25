package com.backend.Skytouch.company.repository;



import com.backend.Skytouch.common.enums.CompanyStatus;

import com.backend.Skytouch.company.entity.Company;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;



import java.util.UUID;



public interface CompanyRepository extends JpaRepository<Company, UUID> {



    Page<Company> findByStatus(CompanyStatus status, Pageable pageable);



    long countByStatus(CompanyStatus status);

}

