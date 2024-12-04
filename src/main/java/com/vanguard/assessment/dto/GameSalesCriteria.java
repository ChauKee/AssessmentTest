package com.vanguard.assessment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class GameSalesCriteria {

    private LocalDate toDate;
    private LocalDate fromDate;
    private Integer gameNo;
    private BigDecimal fromSalePrice;
    private BigDecimal toSalePrice;

}
