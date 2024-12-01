package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.GameMonthlySales;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Repository
public interface GameMonthlySalesRepository extends JpaRepository<GameMonthlySales, LocalDate> {

    @Autowired
    JdbcTemplate jdbcTemplate = null;

    List<GameMonthlySales> findByGameNoAndDateBetweenOrderByDate(Integer gameNo, LocalDate fromDate, LocalDate toDate);

    default List<GameMonthlySales> findByGameNo(String gameNo) {
        return List.of();
    }
}
