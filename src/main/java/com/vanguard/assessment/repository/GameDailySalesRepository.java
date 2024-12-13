package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.GameDailySales;
import com.vanguard.assessment.entity.TotalDailySales;
import com.vanguard.assessment.utils.AppContextUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface GameDailySalesRepository extends JpaRepository<GameDailySales, LocalDate> {

    String SELECT_SQL = "select date, game_no, total_sales, quantity_sold from daily_game_sales where game_no = ? and date >= ? and date <= ? order by date";

    List<GameDailySales> findByGameNoAndDateBetweenOrderByDate(Integer gameNo, LocalDate fromDate, LocalDate toDate);

    default List<GameDailySales> jdbcFindByGameNoAndDateBetweenOrderByDate(Integer gameNo, LocalDate fromDate, LocalDate toDate) {
        JdbcTemplate jdbcTemplate = AppContextUtils.getAppContext().getBean(JdbcTemplate.class);
        return jdbcTemplate.query(SELECT_SQL, pss -> {
            pss.setInt(1, gameNo);
            pss.setObject(2, fromDate);
            pss.setObject(3, toDate);
        }, rs -> {
            List<GameDailySales> results = new ArrayList<>();
            while (rs.next()) {
                GameDailySales result = new GameDailySales();
                result.setDate(rs.getObject(1, LocalDate.class));
                result.setGameNo(rs.getInt(2));
                result.setTotalSales(rs.getBigDecimal(3));
                result.setQuantitySold(rs.getLong(4));
                results.add(result);
            }
            return results;
        });
    }
}
