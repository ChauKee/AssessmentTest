package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.GameMonthlySales;
import com.vanguard.assessment.entity.TotalMonthlySales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TotalMonthlySalesRepository extends JpaRepository<TotalMonthlySales, LocalDate> {

    long countByDateAfterAndDateBefore(LocalDate fromDate, LocalDate toDate);

    List<TotalMonthlySales> findByDateBetweenOrderByDate(LocalDate fromDate, LocalDate toDate);

}
