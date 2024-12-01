package com.vanguard.assessment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter @Setter
public class AggregateGameSalesQueryResult<T> {

    private String fromDate;
    private String toDate;
    private Integer gameNo;
    private BigDecimal totalSales;
    private long quantitySold;
    private List<T> data;

}
