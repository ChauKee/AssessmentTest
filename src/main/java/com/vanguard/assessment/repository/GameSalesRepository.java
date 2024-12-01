package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.GameSales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameSalesRepository extends JpaRepository<GameSales, Integer> {

    Page<GameSales> findBySalePriceBetweenAndDateOfSaleBetweenOrderById(
            BigDecimal salePriceStart, BigDecimal salePriceEnd,
            LocalDateTime dateOfSaleStart, LocalDateTime dateOfSaleEnd, Pageable page);

//    List<GameSales> findBySalePriceBetween(BigDecimal salePriceStart,
//                                         BigDecimal salePriceEnd,
//                                         Pageable page);

}
