package com.backend.Skytouch.common.profile;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProfileCompleteness {

    private final int percentComplete;
    private final List<ProfileStep> steps;
}
