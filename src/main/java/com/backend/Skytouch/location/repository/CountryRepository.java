package com.backend.Skytouch.location.repository;

import com.backend.Skytouch.location.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Integer> {

    List<Country> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Country> findAllByOrderByNameAsc();
}
