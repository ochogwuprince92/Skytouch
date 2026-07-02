package com.backend.Skytouch.location.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CountryResponse {

    private final Integer id;
    private final String name;
    private final String iso2;
    private final String iso3;
    private final String phoneCode;
    private final String currency;
    private final String currencySymbol;
    private final String emoji;
    private final String region;
    private final String subregion;
    private final boolean hasStates;
}
