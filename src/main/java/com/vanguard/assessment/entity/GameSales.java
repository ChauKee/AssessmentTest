package com.vanguard.assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(schema = "vanguard", name = "game_sales")
public class GameSales extends BaseEntity {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "game_no")
    private Integer gameNo;
    @Column(name = "game_name")
    private String gameName;
    @Column(name = "game_code")
    private String gameCode;
    @Column(name = "type")
    private Integer type;
    @Column(name = "cost_price")
    private BigDecimal costPrice;
    @Column(name = "tax")
    private BigDecimal tax;
    @Column(name = "sale_price")
    private BigDecimal salePrice;
    @Column(name = "date_of_sale")
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

