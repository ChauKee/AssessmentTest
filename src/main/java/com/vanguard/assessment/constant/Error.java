package com.vanguard.assessment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Error {

    VALIDATION_ERROR("Validation Error"),
    INVALID_DATE_FORMAT("Date format must be yyyy-MM-dd"),
;
    private final String description;

}
