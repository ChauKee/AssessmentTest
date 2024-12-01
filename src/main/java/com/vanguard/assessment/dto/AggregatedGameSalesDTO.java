package com.vanguard.assessment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AggregatedGameSalesDTO {

    private String date;
    private Integer gameNo;
    private String gameName;
    private BigDecimal totalSales;
    private Long quantitySold;
}
