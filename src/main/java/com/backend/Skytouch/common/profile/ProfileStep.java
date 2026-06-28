package com.backend.Skytouch.common.profile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileStep {

    private final String key;
    private final String label;
    private final boolean complete;
}
