package com.vanguard.assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "monthly_game_sales")
@Getter @Setter
public class GameMonthlySales {

    @Id
    private LocalDate date;
    @Column(name = "game_no")
    private Integer gameNo;
    @Column(name = "total_sales")
    private BigDecimal totalSales;
    @Column(name = "quantity_sold")
    private Long quantitySold;

}
