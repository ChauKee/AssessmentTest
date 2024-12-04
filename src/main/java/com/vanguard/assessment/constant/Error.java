package com.vanguard.assessment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Error {

    VALIDATION_ERROR("Validation Error"),
    ;
    private final String description;

}
