package com.vanguard.assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sales")
public class GameSales extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "game_no", nullable = false)
    private Integer gameNo;
    @Column(name = "game_name", nullable = false, length = 32)
    private String gameName;
    @Column(name = "game_code", nullable = false, length = 10)
    private String gameCode;
    @Column(name = "type", nullable = false)
    private Integer type;
    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;
    @Column(name = "tax", nullable = false)
    private BigDecimal tax;
    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;
    @Column(name = "date_of_sale", nullable = false)
    private LocalDateTime dateOfSale;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGameNo() {
        return gameNo;
    }

    public void setGameNo(Integer gameNo) {
        this.gameNo = gameNo;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public LocalDateTime getDateOfSale() {
        return dateOfSale;
    }

    public void setDateOfSale(LocalDateTime dateOfSale) {
        this.dateOfSale = dateOfSale;
    }
}

