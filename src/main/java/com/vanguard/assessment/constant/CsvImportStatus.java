package com.vanguard.assessment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CsvImportStatus {

    PARSING(1),
    SAVING(2),
    COMPLETED(3),
    FAILED(4),
    ;
    private final int code;

}
