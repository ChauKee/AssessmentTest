package com.vanguard.assessment.service.impl;

import com.vanguard.assessment.constant.Game;
import com.vanguard.assessment.constant.GameType;
import com.vanguard.assessment.dto.*;
import com.vanguard.assessment.entity.GameDailySales;
import com.vanguard.assessment.entity.GameSales;
import com.vanguard.assessment.entity.TotalDailySales;
import com.vanguard.assessment.repository.GameDailySalesRepository;
import com.vanguard.assessment.repository.GameSalesRepository;
import com.vanguard.assessment.repository.TotalDailySalesRepository;
import com.vanguard.assessment.service.GameSalesService;
import com.vanguard.assessment.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.vanguard.assessment.utils.DateTimeUtils.DATE_FORMATTER;
import static com.vanguard.assessment.utils.DateTimeUtils.DATE_TIME_FORMATTER;

@Service
public class GameSalesServiceImpl implements GameSalesService {

    private GameSalesRepository gameSalesRepository;

    private GameDailySalesRepository gameDailySalesRepository;

    private TotalDailySalesRepository totalDailySalesRepository;


    @Override
    public GameSalesQueryResult<GameSalesDTO> getGameSales(GameSalesCriteria criteria, Pageable page) {
        // if fromSalePrice is missing, we use 0 (inclusive) as fromSalePrice
        BigDecimal salePriceStart = Objects.isNull(criteria.getFromSalePrice()) ?
                BigDecimal.ZERO : criteria.getFromSalePrice();
        // if toSalePrice is missing, we use Integer.MAX_VALUE as toSalePrice
        BigDecimal salePriceEnd = Objects.isNull(criteria.getToSalePrice()) ?
                BigDecimal.valueOf(Integer.MAX_VALUE) : criteria.getToSalePrice();

        // if toDate is missing, by default we use today as toDate
        LocalDate toDate = Objects.isNull(criteria.getToDate()) ? LocalDate.now() : criteria.getToDate();
        // if fromDate is missing, by default we use 1 week before from toDate
        LocalDate fromDate = Objects.isNull(criteria.getFromDate()) ? toDate.minusDays(7) : criteria.getFromDate();

        Page<GameSales> results = criteria.isJdbc() ?
                gameSalesRepository.jdbcFindBySalePriceBetweenAndDateOfSaleBetweenOrderById(salePriceStart, salePriceEnd,fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay().minusNanos(1), page) :
                gameSalesRepository.findBySalePriceBetweenAndDateOfSaleBetweenOrderById(
                salePriceStart, salePriceEnd, fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay().minusNanos(1), page);
        List<GameSalesDTO> gameSales = results.stream().map(GameSalesServiceImpl::toDTO).toList();
        GameSalesQueryResult<GameSalesDTO> result = GameSalesQueryResult.<GameSalesDTO>builder()
                .data(gameSales)
                .fromDate(fromDate.format(DateTimeUtils.DATE_FORMATTER)) // return resolved fromDate to FE
                .toDate(toDate.format(DateTimeUtils.DATE_FORMATTER)) // return resolved toDate to FE
                .fromSalePrice(criteria.getFromSalePrice())
                .toSalePrice(criteria.getToSalePrice())
                .page(PageDTO.builder()
                        .page(results.getNumber() + 1) // add 1 back because FE is 1-based, backend is zero-based
                        .pageSize(results.getSize())
                        .totalPages(results.getTotalPages())
                        .total(results.getTotalElements())
                        .build()) // return page object for FE pagination feature
                .isJdbc(criteria.isJdbc())
                .build();

        return result;
    }

    private static GameSalesDTO toDTO(GameSales gameSales) {
        return GameSalesDTO.builder()
                .id(gameSales.getId())
                .gameName(gameSales.getGameName())
                .gameNo(gameSales.getGameNo())
                .type(GameType.fromCode(gameSales.getType())
                        .map(GameType::getDescription)
                        .orElseThrow(() -> new IllegalArgumentException("Illegal gameType - " + gameSales.getType())))
                .salePrice(gameSales.getSalePrice())
                .costPrice(gameSales.getCostPrice())
                .dateOfSale(gameSales.getDateOfSale().format(DATE_TIME_FORMATTER))
                .build();
    }

    @Override
    public AggregateGameSalesQueryResult<AggregatedGameSalesDTO> getTotalSales(GameSalesCriteria criteria) {

        LocalDate fromDate = criteria.getFromDate();
        LocalDate toDate = criteria.getToDate();
        Integer gameNo = criteria.getGameNo();

        AggregateGameSalesQueryResult.AggregateGameSalesQueryResultBuilder<AggregatedGameSalesDTO> builder =
                AggregateGameSalesQueryResult.<AggregatedGameSalesDTO>builder()
                        .fromDate(Objects.nonNull(fromDate) ? fromDate.format(DATE_FORMATTER) : null)
                        .toDate(Objects.nonNull(toDate) ? toDate.format(DATE_FORMATTER) : null)
                        .gameNo(gameNo)
                        .isJdbc(criteria.isJdbc());

        toDate = Objects.isNull(toDate) ? LocalDate.now() : toDate;
        fromDate = Objects.isNull(fromDate) ? toDate.minusDays(7) : fromDate;
        if (Objects.nonNull(gameNo)) {
            List<GameDailySales> results = criteria.isJdbc() ?
                    gameDailySalesRepository.jdbcFindByGameNoAndDateBetweenOrderByDate(gameNo, fromDate, toDate) :
                    gameDailySalesRepository.findByGameNoAndDateBetweenOrderByDate(gameNo, fromDate, toDate);
            List<AggregatedGameSalesDTO> gameDailySales = results.stream().map(this::toAggregateDTO).toList();
            builder.data(gameDailySales);
            builder.totalSales(gameDailySales.stream().map(AggregatedGameSalesDTO::getTotalSales).reduce(BigDecimal.ZERO, BigDecimal::add));
            builder.quantitySold(gameDailySales.stream().map(AggregatedGameSalesDTO::getQuantitySold).reduce(0L, Long::sum));
        } else {
            List<TotalDailySales> results = criteria.isJdbc() ? totalDailySalesRepository.jdbcFindByDateBetweenOrderByDate(
                    fromDate, toDate) : totalDailySalesRepository.findByDateBetweenOrderByDate(fromDate, toDate);
            List<AggregatedGameSalesDTO> gameDailySales = results.stream().map(this::toAggregateDTO).toList() ;
            builder.data(gameDailySales);
            builder.totalSales(gameDailySales.stream().map(AggregatedGameSalesDTO::getTotalSales).reduce(BigDecimal.ZERO, BigDecimal::add));
            builder.quantitySold(gameDailySales.stream().map(AggregatedGameSalesDTO::getQuantitySold).reduce(0L, Long::sum));
        }
        builder.fromDate(fromDate.format(DateTimeUtils.DATE_FORMATTER));
        builder.toDate(toDate.format(DateTimeUtils.DATE_FORMATTER));
        return builder.build();
    }


    private AggregatedGameSalesDTO toAggregateDTO(TotalDailySales totalDailySales) {
        return AggregatedGameSalesDTO.builder()
                .gameName(null)
                .gameNo(null)
                .date(totalDailySales.getDate().format(DATE_FORMATTER))
                .totalSales(totalDailySales.getTotalSales())
                .quantitySold(totalDailySales.getQuantitySold())
                .build();
    }

    private AggregatedGameSalesDTO toAggregateDTO(GameDailySales gameDailySales) {
        return AggregatedGameSalesDTO.builder()
                .gameNo(gameDailySales.getGameNo())
                .gameName(resolveGameNameFromGameNo(gameDailySales.getGameNo()))
                .date(gameDailySales.getDate().format(DATE_FORMATTER))
                .totalSales(gameDailySales.getTotalSales())
                .quantitySold(gameDailySales.getQuantitySold())
                .build();
    }

    protected String resolveGameNameFromGameNo(Integer gameNo) {
        return Game.fromGameNo(gameNo)
                .map(Game::getName)
                .orElseThrow();
    }

    @Autowired
    public void setGameSalesRepository(GameSalesRepository gameSalesRepository) {
        this.gameSalesRepository = gameSalesRepository;
    }

    @Autowired
    public void setGameDailySalesRepository(GameDailySalesRepository gameDailySalesRepository) {
        this.gameDailySalesRepository = gameDailySalesRepository;
    }

    @Autowired
    public void setTotalDailySalesRepository(TotalDailySalesRepository totalDailySalesRepository) {
        this.totalDailySalesRepository = totalDailySalesRepository;
    }

}
