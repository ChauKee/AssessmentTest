package com.vanguard.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vanguard.assessment.dto.*;
import com.vanguard.assessment.entity.GameDailySales;
import com.vanguard.assessment.entity.GameSales;
import com.vanguard.assessment.entity.TotalDailySales;
import com.vanguard.assessment.repository.GameDailySalesRepository;
import com.vanguard.assessment.repository.GameSalesRepository;
import com.vanguard.assessment.repository.TotalDailySalesRepository;
import com.vanguard.assessment.service.impl.GameSalesServiceImpl;
import com.vanguard.assessment.utils.CSVParser;
import com.vanguard.assessment.utils.DateTimeUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTest.ServiceTestConfig.class)
public class ServiceTest {

    @Autowired
    private GameSalesService gameSalesService;

    private static final List<GameSales> gameSalesRecords = new ArrayList<>();

    private static final String[] HEADERS = {
            "id", "game_no", "game_name", "game_code", "type",
            "cost_price", "tax", "sale_price", "date_of_sale"
    };

    @BeforeAll
    public static void init() throws Exception {
        ClassPathResource cpr = new ClassPathResource("test_game_sales.csv");
        List<Map<String, Object>> results = CSVParser.parse(cpr.getInputStream(), HEADERS, (header, value) -> {
            if ("id".equals(header))
                return Long.parseLong(value);
            if ("game_no".equals(header))
                return Integer.parseInt(value);
            if ("game_name".equals(header))
                return value;
            if ("game_code".equals(header))
                return value;
            if ("type".equals(header))
                return Integer.parseInt(value);
            if ("cost_price".equals(header))
                return new BigDecimal(value);
            if ("tax".equals(header))
                return new BigDecimal(value.replace("%", ""))
                        .divide(BigDecimal.valueOf(100L));
            if ("sale_price".equals(header))
                return new BigDecimal(value);;
            if ("date_of_sale".equals(header))
                return LocalDateTime.parse(value, DateTimeUtils.DATE_TIME_FORMATTER);
            return value;
        });

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        gameSalesRecords.addAll(results.stream().map(map -> objectMapper.convertValue(map, GameSales.class)).toList());
    }


    @Test
    public void testGetGameSales() {
        BigDecimal fromSalePrice = new BigDecimal("0");
        BigDecimal toSalePrice = new BigDecimal("30");
        LocalDate fromDate = LocalDate.parse("2024-04-01", DateTimeUtils.DATE_FORMATTER);
        LocalDate toDate = LocalDate.parse("2024-04-30", DateTimeUtils.DATE_FORMATTER);

        GameSalesCriteria criteria = new GameSalesCriteria();
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);
        criteria.setFromSalePrice(fromSalePrice);
        criteria.setToSalePrice(toSalePrice);

        PageRequest pageRequest = PageRequest.of(0, gameSalesRecords.size()); // to get all filtered record in one page

        GameSalesQueryResult<GameSalesDTO> results = gameSalesService.getGameSales(criteria, pageRequest);
        List<GameSales> expecteds = gameSalesRecords.stream()
                .filter(gsr -> gsr.getSalePrice().compareTo(fromSalePrice) != -1)
                .filter(gsr -> gsr.getSalePrice().compareTo(toSalePrice) != 1)
                .filter(gsr -> !gsr.getDateOfSale().isBefore(fromDate.atStartOfDay()))
                .filter(gsr -> !gsr.getDateOfSale().isAfter(toDate.plusDays(1).atStartOfDay().minusNanos(1L)))
                .toList();

        assertEquals(expecteds.size(), results.getData().size());

    }

    @Test
    public void testTotalSales() {
        LocalDate fromDate = LocalDate.parse("2024-04-01", DateTimeUtils.DATE_FORMATTER);
        LocalDate toDate = LocalDate.parse("2024-04-30", DateTimeUtils.DATE_FORMATTER);

        GameSalesCriteria criteria = new GameSalesCriteria();
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);

        AggregateGameSalesQueryResult<AggregatedGameSalesDTO> results = gameSalesService.getTotalSales(criteria);

        Map<String, TotalDailySales> expectedMap = gameSalesRecords.stream().collect(Collectors.toMap(gsr ->
                gsr.getDateOfSale().toLocalDate().format(DateTimeUtils.DATE_FORMATTER), gsr-> {
            TotalDailySales gds = new TotalDailySales();
            gds.setDate(gsr.getDateOfSale().toLocalDate());
            gds.setTotalSales(gsr.getSalePrice());
            gds.setQuantitySold(1L);
            return gds;
        }, (tds1, tds2)-> {
            tds1.setTotalSales(tds1.getTotalSales().add(tds2.getTotalSales()));
            tds1.setQuantitySold(tds1.getQuantitySold()+1);
            return tds1;
        }));

        assertTrue(results.getData().stream().allMatch(gds -> {
            String key = gds.getDate().formatted(DateTimeUtils.DATE_FORMATTER);
            TotalDailySales expected = expectedMap.get(key);
            return Objects.equals(expected.getTotalSales(), gds.getTotalSales()) &&
                    Objects.equals(expected.getQuantitySold(), gds.getQuantitySold())  ;
        }));
    }

    @Test
    public void testTotalSalesWithGameNo() {
        Integer gameNo = 1;
        LocalDate fromDate = LocalDate.parse("2024-04-01", DateTimeUtils.DATE_FORMATTER);
        LocalDate toDate = LocalDate.parse("2024-04-30", DateTimeUtils.DATE_FORMATTER);

        GameSalesCriteria criteria = new GameSalesCriteria();
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);
        criteria.setGameNo(gameNo);

        AggregateGameSalesQueryResult<AggregatedGameSalesDTO> results = gameSalesService.getTotalSales(criteria);

        Map<String, GameDailySales> expectedMap = gameSalesRecords.stream().collect(Collectors.toMap(gsr ->
                gsr.getDateOfSale().toLocalDate().format(DateTimeUtils.DATE_FORMATTER) + "_" + gsr.getGameNo(), gsr-> {
            GameDailySales gds = new GameDailySales();
            gds.setDate(gsr.getDateOfSale().toLocalDate());
            gds.setGameNo(gsr.getGameNo());
            gds.setTotalSales(gsr.getSalePrice());
            gds.setQuantitySold(1L);
            return gds;
        }, (gds1, gds2)-> {
            gds1.setTotalSales(gds1.getTotalSales().add(gds2.getTotalSales()));
            gds1.setQuantitySold(gds1.getQuantitySold()+1);
            return gds1;
        }));

        assertTrue(results.getData().stream().allMatch(gds -> {
            String key = gds.getDate().formatted(DateTimeUtils.DATE_FORMATTER) + "_" + gds.getGameNo();
            GameDailySales expected = expectedMap.get(key);
            return Objects.equals(expected.getTotalSales(), gds.getTotalSales()) &&
                    Objects.equals(expected.getQuantitySold(), gds.getQuantitySold())  ;
        }));
    }

    static class ServiceTestConfig {

        @Bean
        GameSalesService gameSalesService() {
            return new GameSalesServiceImpl();
        }

        @Bean
        GameSalesRepository gameSalesRepository() {

            GameSalesRepository mock = Mockito.mock(GameSalesRepository.class);

            Mockito.when(mock.findBySalePriceBetweenAndDateOfSaleBetweenOrderById(Mockito.any(BigDecimal.class),
                            Mockito.any(BigDecimal.class), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                            Mockito.any(Pageable.class) ))
                    .thenAnswer(invocation -> {
                        BigDecimal fromSalePrice = invocation.getArgument(0);
                        BigDecimal toSalePrice = invocation.getArgument(1);
                        LocalDateTime fromDateOfSale = invocation.getArgument(2);
                        LocalDateTime toDateOfSale =  invocation.getArgument(3);
                        Pageable pageable = invocation.getArgument(4);
                        List<GameSales> gameSales = gameSalesRecords.stream()
                                .filter(record -> record.getSalePrice().compareTo(fromSalePrice) >= 0)
                                .filter(record -> record.getSalePrice().compareTo(toSalePrice) <= 0)
                                .filter(record -> record.getDateOfSale().isEqual(fromDateOfSale) || record.getDateOfSale().isAfter(fromDateOfSale))
                                .filter(record -> record.getDateOfSale().isEqual(toDateOfSale) || record.getDateOfSale().isBefore(toDateOfSale))
                                .skip(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .toList();
                        long total = gameSalesRecords.stream()
                                .filter(record -> record.getSalePrice().compareTo(fromSalePrice) == 0)
                                .filter(record -> record.getSalePrice().compareTo(fromSalePrice) == 0)
                                .filter(record -> record.getDateOfSale().isEqual(fromDateOfSale) || record.getDateOfSale().isAfter(fromDateOfSale))
                                .filter(record -> record.getDateOfSale().isEqual(toDateOfSale) || record.getDateOfSale().isBefore(toDateOfSale))
                                .count();
                        return new PageImpl<>(gameSales, pageable, total);
                    });

            return mock;
        }

        @Bean
        GameDailySalesRepository gameDailySalesRepository() {
            GameDailySalesRepository mock = Mockito.mock(GameDailySalesRepository.class);
            Mockito.when(mock.findByGameNoAndDateBetweenOrderByDate(Mockito.anyInt(), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class) ))
                    .thenAnswer(invocation -> {
                        Integer gameNo = invocation.getArgument(0);
                        LocalDate fromDate = invocation.getArgument(1);
                        LocalDate toDate = invocation.getArgument(2);

                        Collection<GameDailySales> gameDailySales = gameSalesRecords.stream().collect(Collectors.toMap(gsr ->gsr.getDateOfSale().toLocalDate().toString() + gsr.getGameNo(), gsr-> {
                            GameDailySales gds = new GameDailySales();
                            gds.setDate(gsr.getDateOfSale().toLocalDate());
                            gds.setGameNo(gsr.getGameNo());
                            gds.setTotalSales(gsr.getSalePrice());
                            gds.setQuantitySold(1L);
                            return gds;
                        }, (gds1, gds2)-> {
                            gds1.setTotalSales(gds1.getTotalSales().add(gds2.getTotalSales()));
                            gds1.setQuantitySold(gds1.getQuantitySold()+1);
                            return gds1;
                        })).values();
                        return gameDailySales.stream()
                                .filter(gds -> !gds.getDate().isBefore(fromDate))
                                .filter(gds -> !gds.getDate().isAfter(toDate))
                                .sorted(Comparator.comparing(GameDailySales::getDate))
                                .toList();
                    });

            return mock;
        }

        @Bean
        TotalDailySalesRepository totalDailySalesRepository() {
            TotalDailySalesRepository mock = Mockito.mock(TotalDailySalesRepository.class);
            Mockito.when(mock.findByDateBetweenOrderByDate(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class) ))
                    .thenAnswer(invocation -> {
                        LocalDate fromDate = invocation.getArgument(0);
                        LocalDate toDate = invocation.getArgument(1);

                        Collection<TotalDailySales> gameDailySales = gameSalesRecords.stream().collect(Collectors.toMap(gsr ->gsr.getDateOfSale().toLocalDate().toString(), gsr-> {
                            TotalDailySales gds = new TotalDailySales();
                            gds.setDate(gsr.getDateOfSale().toLocalDate());
                            gds.setTotalSales(gsr.getSalePrice());
                            gds.setQuantitySold(1L);
                            return gds;
                        }, (gds1, gds2)-> {
                            gds1.setTotalSales(gds1.getTotalSales().add(gds2.getTotalSales()));
                            gds1.setQuantitySold(gds1.getQuantitySold()+1);
                            return gds1;
                        })).values();
                        return gameDailySales.stream()
                                .filter(gds -> !gds.getDate().isBefore(fromDate))
                                .filter(gds -> !gds.getDate().isAfter(toDate))
                                .sorted(Comparator.comparing(TotalDailySales::getDate))
                                .toList();
                    });

            return mock;
        }

    }

}
