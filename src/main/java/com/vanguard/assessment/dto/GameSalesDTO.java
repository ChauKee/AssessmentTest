package com.vanguard.assessment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameSalesDTO {

    private Long id;
    private Integer gameNo;
    private String gameName;
    private String type;
    private BigDecimal costPrice;
    private BigDecimal tax;
    private BigDecimal salePrice;
    private String dateOfSale;


}
