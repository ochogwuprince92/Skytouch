package com.backend.Skytouch.location.service;

import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.LocationMapper;
import com.backend.Skytouch.location.apimodel.CountryResponse;
import com.backend.Skytouch.location.apimodel.StateResponse;
import com.backend.Skytouch.location.entity.Country;
import com.backend.Skytouch.location.repository.CountryRepository;
import com.backend.Skytouch.location.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final LocationMapper locationMapper;

    @Transactional(readOnly = true)
    public List<CountryResponse> listCountries(String search) {
        List<Country> countries = StringUtils.hasText(search)
                ? countryRepository.findByNameContainingIgnoreCaseOrderByNameAsc(search.trim())
                : countryRepository.findAllByOrderByNameAsc();
        return countries.stream().map(locationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CountryResponse getCountry(Integer id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found: " + id));
        return locationMapper.toResponse(country);
    }

    @Transactional(readOnly = true)
    public List<StateResponse> listStatesForCountry(Integer countryId) {
        if (!countryRepository.existsById(countryId)) {
            throw new ResourceNotFoundException("Country not found: " + countryId);
        }
        return stateRepository.findByCountry_IdOrderByNameAsc(countryId).stream()
                .map(locationMapper::toResponse)
                .toList();
    }
}
