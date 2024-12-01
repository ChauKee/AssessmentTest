package com.vanguard.assessment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
public class PageDTO {

    private int page;
    private int pageSize;
    private long total;
    private int totalPages;

}
