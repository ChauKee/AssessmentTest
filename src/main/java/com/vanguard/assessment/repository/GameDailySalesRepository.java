package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.GameDailySales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GameDailySalesRepository extends JpaRepository<GameDailySales, LocalDate> {

    List<GameDailySales> findByGameNoAndDateBetweenOrderByDate(Integer gameNo, LocalDate fromDate, LocalDate toDate);

}
