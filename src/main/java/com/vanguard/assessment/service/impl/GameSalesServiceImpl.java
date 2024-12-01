package com.vanguard.assessment.service.impl;

import com.vanguard.assessment.constant.Game;
import com.vanguard.assessment.constant.GameType;
import com.vanguard.assessment.dto.*;
import com.vanguard.assessment.entity.*;
import com.vanguard.assessment.repository.*;
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


    @Override
    public GameSalesQueryResult<GameSalesDTO> getGameSales(GameSalesCriteria criteria, Pageable page) {
        BigDecimal salePriceStart = Objects.isNull(criteria.getFromSalePrice()) ?
                BigDecimal.ZERO : criteria.getFromSalePrice();
        BigDecimal salePriceEnd = Objects.isNull(criteria.getToSalePrice()) ?
                BigDecimal.valueOf(Integer.MAX_VALUE) : criteria.getToSalePrice();

        LocalDate toDate = Objects.isNull(criteria.getToDate()) ? LocalDate.now() : criteria.getToDate();
        LocalDate fromDate = Objects.isNull(criteria.getFromDate()) ? toDate.minusDays(7) : criteria.getFromDate();

        Page<GameSales> results = gameSalesRepository.findBySalePriceBetweenAndDateOfSaleBetweenOrderById(
                salePriceStart, salePriceEnd, fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay().minusNanos(1), page);
        List<GameSalesDTO> dtos = results.stream().map(GameSalesServiceImpl::toDTO).toList();
        GameSalesQueryResult<GameSalesDTO> result = GameSalesQueryResult.<GameSalesDTO>builder()
                .data(dtos)
                .fromDate(Objects.nonNull(criteria.getFromDate()) ? criteria.getFromDate().format(DateTimeUtils.DATE_FORMATTER) : null)
                .toDate(Objects.nonNull(criteria.getToDate()) ? criteria.getToDate().format(DateTimeUtils.DATE_FORMATTER) : null)
                .page(PageDTO.builder()
                        .page(results.getNumber() + 1)
                        .pageSize(results.getSize())
                        .totalPages(results.getTotalPages())
                        .total(results.getTotalElements())
                        .build())
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
        Integer frequency = criteria.getFrequency();
        AggregateGameSalesQueryResult.AggregateGameSalesQueryResultBuilder<AggregatedGameSalesDTO> builder =
                AggregateGameSalesQueryResult.<AggregatedGameSalesDTO>builder()
                        .fromDate(Objects.nonNull(fromDate) ? fromDate.format(DATE_FORMATTER) : null)
                        .toDate(Objects.nonNull(toDate) ? toDate.format(DATE_FORMATTER) : null)
                        .gameNo(gameNo);

//        if (frequency == 1) {
            toDate = Objects.isNull(toDate) ? LocalDate.now() : toDate;
            fromDate = Objects.isNull(fromDate) ? toDate.minusDays(7) : fromDate;
            if (Objects.nonNull(gameNo)) {
                List<AggregatedGameSalesDTO> gameDailySales = gameDailySalesRepository.findByGameNoAndDateBetweenOrderByDate(
                        gameNo, fromDate, toDate).stream().map(this::toAggregateDTO).toList();
                builder.data(gameDailySales);
                builder.totalSales(gameDailySales.stream().map(AggregatedGameSalesDTO::getTotalSales).reduce(BigDecimal.ZERO, BigDecimal::add));
                builder.quantitySold(gameDailySales.stream().map(AggregatedGameSalesDTO::getQuantitySold).reduce(0L, Long::sum));
            } else {
                List<AggregatedGameSalesDTO> gameDailySales = gameDailySalesRepository.findByDateBetweenOrderByDate(
                        fromDate, toDate).stream().map(this::toAggregateDTO).toList();
                builder.data(gameDailySales);
                builder.totalSales(gameDailySales.stream().map(AggregatedGameSalesDTO::getTotalSales).reduce(BigDecimal.ZERO, BigDecimal::add));
                builder.quantitySold(gameDailySales.stream().map(AggregatedGameSalesDTO::getQuantitySold).reduce(0L, Long::sum));
            }
//        } else if (frequency == 2) {
//            toDate = Objects.isNull(toDate) ? LocalDate.now() : toDate;
//            fromDate = Objects.isNull(fromDate) ? toDate.minusMonths(3) : fromDate;
//            if (Objects.nonNull(gameNo)) {
//                List<AggregatedGameSalesDTO> gameDailySales = gameMonthlySalesRepository.findByGameNoAndDateBetweenOrderByDate(
//                        gameNo, fromDate, toDate).stream().map(this::toAggregateDTO).toList();
//                builder.data(gameDailySales);
//                builder.totalSales(gameDailySales.stream().map(AggregatedGameSalesDTO::getTotalSales).reduce(BigDecimal.ZERO, BigDecimal::add));
//                builder.quantitySold(gameDailySales.stream().map(AggregatedGameSalesDTO::getQuantitySold).reduce(0L, Long::sum));
//            } else {
//                List<AggregatedGameSalesDTO> gameDailySales = totalMonthlySalesRepository.findByDateBetweenOrderByDate(
//                        fromDate, toDate).stream().map(this::toAggregateDTO).toList();
//                builder.data(gameDailySales);
//                builder.totalSales(gameDailySales.stream().map(AggregatedGameSalesDTO::getTotalSales).reduce(BigDecimal.ZERO, BigDecimal::add));
//                builder.quantitySold(gameDailySales.stream().map(AggregatedGameSalesDTO::getQuantitySold).reduce(0L, Long::sum));
//            }
//        }
        return builder.build();
    }


//    private AggregatedGameSalesDTO toAggregateDTO(TotalDailySales totalDailySales) {
//        return AggregatedGameSalesDTO.builder()
//                .gameName(null)
//                .gameNo(null)
//                .date(totalDailySales.getDate().format(DATE_FORMATTER))
//                .totalSales(totalDailySales.getTotalSales())
//                .quantitySold(totalDailySales.getQuantitySold())
//                .build();
//    }

    private AggregatedGameSalesDTO toAggregateDTO(GameDailySales gameDailySales) {
        return AggregatedGameSalesDTO.builder()
                .gameNo(gameDailySales.getGameNo())
                .gameName(resolveGameNameFromGameNo(gameDailySales.getGameNo()))
                .date(gameDailySales.getDate().format(DATE_FORMATTER))
                .totalSales(gameDailySales.getTotalSales())
                .quantitySold(gameDailySales.getQuantitySold())
                .build();
    }

//    private AggregatedGameSalesDTO toAggregateDTO(TotalMonthlySales totalMonthlySales) {
//        return AggregatedGameSalesDTO.builder()
//                .gameNo(null)
//                .gameName(null)
//                .date(totalMonthlySales.getDate().format(DATE_FORMATTER))
//                .totalSales(totalMonthlySales.getTotalSales())
//                .quantitySold(totalMonthlySales.getQuantitySold())
//                .build();
//    }
//
//    private AggregatedGameSalesDTO toAggregateDTO(GameMonthlySales gameMonthlySales) {
//        return AggregatedGameSalesDTO.builder()
//                .gameNo(gameMonthlySales.getGameNo())
//                .gameName(resolveGameNameFromGameNo(gameMonthlySales.getGameNo()))
//                .date(gameMonthlySales.getDate().format(DATE_FORMATTER))
//                .totalSales(gameMonthlySales.getTotalSales())
//                .quantitySold(gameMonthlySales.getQuantitySold())
//                .build();
//    }

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

}
