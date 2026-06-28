package com.backend.Skytouch.common.address;

import com.backend.Skytouch.common.config.AddressValidationProperties;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressValidationService {

    private final AddressValidationProperties properties;
    private final RestClient restClient = RestClient.create();

    public ValidatedAddress validate(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }

        String trimmed = address.trim();
        if (!properties.isEnabled()) {
            return ValidatedAddress.fromRaw(trimmed);
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BadRequestException("Address validation is enabled but GOOGLE_ADDRESS_API_KEY is not configured");
        }

        URI uri = UriComponentsBuilder.fromUriString(properties.getEndpoint())
                .queryParam("address", trimmed)
                .queryParam("key", properties.getApiKey())
                .build()
                .encode()
                .toUri();

        GeocodeResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(GeocodeResponse.class);

        if (response == null || response.results() == null || response.results().isEmpty()) {
            throw new BadRequestException("Address could not be validated: " + mapStatus(response));
        }

        if (!"OK".equalsIgnoreCase(response.status())) {
            throw new BadRequestException("Address could not be validated: " + mapStatus(response));
        }

        GeocodeResult result = response.results().getFirst();
        return new ValidatedAddress(
                result.formattedAddress(),
                findComponent(result.addressComponents(), "administrative_area_level_2", "locality", "sublocality"),
                findComponent(result.addressComponents(), "administrative_area_level_1")
        );
    }

    private String mapStatus(GeocodeResponse response) {
        if (response == null || !StringUtils.hasText(response.status())) {
            return "no results";
        }
        return switch (response.status()) {
            case "ZERO_RESULTS" -> "address not found";
            case "INVALID_REQUEST" -> "invalid address";
            case "REQUEST_DENIED" -> "address validation service denied the request";
            case "OVER_QUERY_LIMIT" -> "address validation quota exceeded";
            default -> response.status();
        };
    }

    private String findComponent(List<AddressComponent> components, String... types) {
        if (components == null) {
            return null;
        }
        for (String type : types) {
            for (AddressComponent component : components) {
                if (component.types() != null && component.types().contains(type)) {
                    return component.longName();
                }
            }
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodeResponse(String status, List<GeocodeResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodeResult(String formatted_address, List<AddressComponent> address_components) {

        String formattedAddress() {
            return formatted_address;
        }

        List<AddressComponent> addressComponents() {
            return address_components;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AddressComponent(String long_name, List<String> types) {

        String longName() {
            return long_name;
        }
    }
}
