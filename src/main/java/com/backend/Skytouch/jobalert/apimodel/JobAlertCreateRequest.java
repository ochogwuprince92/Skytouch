package com.backend.Skytouch.jobalert.apimodel;

import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.WorkMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobAlertCreateRequest {

    private String name;
    private String keyword;
    private EmploymentType employmentType;
    private WorkMode workMode;
    private String locationState;
    private String industry;
}
