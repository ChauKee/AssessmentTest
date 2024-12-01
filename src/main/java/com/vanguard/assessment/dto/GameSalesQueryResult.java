package com.vanguard.assessment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameSalesQueryResult<T> {

    private String error;
    private String errorMessage;
    private PageDTO page;
    private List<T> data;
    private String fromDate;
    private String toDate;
    private String fromSalePrice;
    private String toSalePrice;

}
