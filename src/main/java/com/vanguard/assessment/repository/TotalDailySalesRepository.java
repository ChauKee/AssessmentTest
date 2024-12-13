package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.TotalDailySales;
import com.vanguard.assessment.utils.AppContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface TotalDailySalesRepository extends JpaRepository<TotalDailySales, LocalDate> {

    String SELECT_SQL = "select date, total_sales, quantity_sold from daily_sales where date >= ? and date <= ? order by date";

    List<TotalDailySales> findByDateBetweenOrderByDate(LocalDate fromDate, LocalDate toDate);

    default List<TotalDailySales> jdbcFindByDateBetweenOrderByDate(LocalDate fromDate, LocalDate toDate) {
        JdbcTemplate jdbcTemplate = AppContextUtils.getAppContext().getBean(JdbcTemplate.class);
        return jdbcTemplate.query(SELECT_SQL, pss -> {
            pss.setObject(1, fromDate);
            pss.setObject(2, toDate);
        }, rs -> {
            List<TotalDailySales> results = new ArrayList<>();
            while (rs.next()) {
                TotalDailySales result = new TotalDailySales();
                result.setDate(rs.getObject(1, LocalDate.class));
                result.setTotalSales(rs.getBigDecimal(2));
                result.setQuantitySold(rs.getLong(3));
                results.add(result);
            }
            return results;
//            return results.stream().sorted(Comparator.comparing(TotalDailySales::getDate)).toList();
        });
    }
}
