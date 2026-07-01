package com.backend.Skytouch.offer.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OfferExpiryRunResponse {

    private final int offersExpired;
}
