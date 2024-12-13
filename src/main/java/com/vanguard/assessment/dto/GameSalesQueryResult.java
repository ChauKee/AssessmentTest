package com.vanguard.assessment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    private BigDecimal fromSalePrice;
    private BigDecimal toSalePrice;
    private Boolean isJdbc;
}
