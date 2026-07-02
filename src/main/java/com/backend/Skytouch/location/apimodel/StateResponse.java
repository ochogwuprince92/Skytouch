package com.backend.Skytouch.location.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StateResponse {

    private final Integer id;
    private final Integer countryId;
    private final String name;
    private final String stateCode;
    private final boolean hasCities;
}
