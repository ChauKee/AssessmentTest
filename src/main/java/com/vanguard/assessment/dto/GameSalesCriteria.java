package com.vanguard.assessment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class GameSalesCriteria {

    private LocalDate toDate;
    private LocalDate fromDate;
    private BigDecimal salePrice;
    private Integer gameNo;
    private Integer frequency;
    private BigDecimal fromSalePrice;
    private BigDecimal toSalePrice;

}
