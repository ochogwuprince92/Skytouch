package com.backend.Skytouch.common.address;

public record ValidatedAddress(String addressLine, String lga, String state) {

    public static ValidatedAddress fromRaw(String addressLine) {
        return new ValidatedAddress(addressLine.trim(), null, null);
    }
}
