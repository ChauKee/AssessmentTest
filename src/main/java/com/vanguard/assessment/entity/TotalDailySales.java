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
@Table(name = "daily_sales")
@Getter @Setter
public class TotalDailySales {
    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "total_sales", nullable = false)
    private BigDecimal totalSales;
    @Column(name = "quantity_sold", nullable = false)
    private Long quantitySold;

}
