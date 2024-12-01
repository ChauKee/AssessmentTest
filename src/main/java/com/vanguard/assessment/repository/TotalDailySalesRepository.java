package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.TotalDailySales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TotalDailySalesRepository extends JpaRepository<TotalDailySales, LocalDate> {

    long countByDateAfterAndDateBefore(LocalDate fromDate, LocalDate toDate);

    List<TotalDailySales> findByDateBetweenOrderByDate(LocalDate fromDate, LocalDate toDate);

}
