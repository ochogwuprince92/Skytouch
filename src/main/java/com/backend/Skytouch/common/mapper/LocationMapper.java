package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.location.apimodel.CountryResponse;
import com.backend.Skytouch.location.apimodel.StateResponse;
import com.backend.Skytouch.location.entity.Country;
import com.backend.Skytouch.location.entity.State;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public CountryResponse toResponse(Country country) {
        return CountryResponse.builder()
                .id(country.getId())
                .name(country.getName())
                .iso2(country.getIso2())
                .iso3(country.getIso3())
                .phoneCode(country.getPhoneCode())
                .currency(country.getCurrency())
                .currencySymbol(country.getCurrencySymbol())
                .emoji(country.getEmoji())
                .region(country.getRegion())
                .subregion(country.getSubregion())
                .hasStates(country.isHasStates())
                .build();
    }

    public StateResponse toResponse(State state) {
        return StateResponse.builder()
                .id(state.getId())
                .countryId(state.getCountry().getId())
                .name(state.getName())
                .stateCode(state.getStateCode())
                .hasCities(state.isHasCities())
                .build();
    }
}
