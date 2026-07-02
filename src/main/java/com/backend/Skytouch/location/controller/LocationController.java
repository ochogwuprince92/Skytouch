package com.backend.Skytouch.location.controller;

import com.backend.Skytouch.location.apimodel.CountryResponse;
import com.backend.Skytouch.location.apimodel.StateResponse;
import com.backend.Skytouch.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public List<CountryResponse> listCountries(
            @RequestParam(required = false) String search) {
        return locationService.listCountries(search);
    }

    @GetMapping("/{id}")
    public CountryResponse getCountry(@PathVariable Integer id) {
        return locationService.getCountry(id);
    }

    @GetMapping("/{id}/states")
    public List<StateResponse> listStates(@PathVariable Integer id) {
        return locationService.listStatesForCountry(id);
    }
}
