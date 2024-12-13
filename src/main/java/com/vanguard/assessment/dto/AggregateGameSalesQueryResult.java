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
public class AggregateGameSalesQueryResult<T> {

    private String error;
    private String errorMessage;
    private String fromDate;
    private String toDate;
    private Integer gameNo;
    private BigDecimal totalSales;
    private long quantitySold;
    private List<T> data;
    private Boolean isJdbc;

}
