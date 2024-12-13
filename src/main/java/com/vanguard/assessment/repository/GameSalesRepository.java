package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.GameSales;
import com.vanguard.assessment.utils.AppContextUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Repository
public interface GameSalesRepository extends JpaRepository<GameSales, Integer> {

    String FROM_WHERE_SQL = "from game_sales where DATE_OF_SALE >= ? and DATE_OF_SALE <= ? and " +
            " SALE_PRICE >= ? and SALE_PRICE <= ? ";

    String SELECT_SQL = "select ID,GAME_NO, GAME_NAME,GAME_CODE, TYPE, COST_PRICE, TAX, SALE_PRICE, DATE_OF_SALE, CREATED_DATE, CREATED_BY, UPDATED_DATE, UPDATED_BY " + FROM_WHERE_SQL + " order by ID limit ? offset ?";

    String SELECT_COUNT_SQL = "select COUNT(ID) " + FROM_WHERE_SQL;

    Page<GameSales> findBySalePriceBetweenAndDateOfSaleBetweenOrderById(
            BigDecimal salePriceStart, BigDecimal salePriceEnd,
            LocalDateTime dateOfSaleStart, LocalDateTime dateOfSaleEnd, Pageable page);

    default Page<GameSales> jdbcFindBySalePriceBetweenAndDateOfSaleBetweenOrderById(
            BigDecimal salePriceStart, BigDecimal salePriceEnd,
            LocalDateTime dateOfSaleStart, LocalDateTime dateOfSaleEnd, Pageable page) {
        JdbcTemplate jdbcTemplate = AppContextUtils.getAppContext().getBean(JdbcTemplate.class);

        Future<Long> totalResultsFuture = CompletableFuture.supplyAsync(() -> jdbcTemplate.queryForObject(SELECT_COUNT_SQL,
                new Object[]{dateOfSaleStart, dateOfSaleEnd, salePriceStart, salePriceEnd}, Long.class));
        List<GameSales> gameSalesList = jdbcTemplate.query(SELECT_SQL, pss -> {
            pss.setObject(1, dateOfSaleStart);
            pss.setObject(2, dateOfSaleEnd);
            pss.setBigDecimal(3, salePriceStart);
            pss.setBigDecimal(4, salePriceEnd);
            pss.setInt(5, page.getPageSize());
            pss.setLong(6, page.getOffset());
        }, rs -> {
            List<GameSales> results = new ArrayList<>();
            while (rs.next()) {
                GameSales result = new GameSales();
                result.setId(rs.getLong("id"));
                result.setGameNo(rs.getInt("game_no"));
                result.setGameName(rs.getString("game_name"));
                result.setGameCode(rs.getString("game_code"));
                result.setType(rs.getInt("type"));
                result.setCostPrice(rs.getBigDecimal("cost_price"));
                result.setTax(rs.getBigDecimal("tax"));
                result.setSalePrice(rs.getBigDecimal("sale_price"));
                result.setDateOfSale(rs.getObject("date_of_sale", LocalDateTime.class));
                result.setCreatedDate(rs.getTimestamp("created_date"));
                result.setCreatedBy(rs.getString("created_by"));
                result.setUpdatedDate(rs.getTimestamp("updated_date"));
                result.setUpdatedBy(rs.getString("updated_by"));
                results.add(result);
            }
            return results;
        });
        try {
            return new PageImpl<>(gameSalesList, page, totalResultsFuture.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
