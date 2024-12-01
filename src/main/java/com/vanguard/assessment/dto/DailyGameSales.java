package com.vanguard.assessment.dto;

import com.vanguard.assessment.constant.AggregateType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter @Setter
public class DailyGameSales extends AggregateGameSales {

    private String aggregateType = AggregateType.DAILY.getValue();

}
