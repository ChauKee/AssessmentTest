package com.vanguard.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vanguard.assessment.dto.GameSalesCriteria;
import com.vanguard.assessment.entity.GameSales;
import com.vanguard.assessment.repository.*;
import com.vanguard.assessment.service.impl.GameSalesServiceImpl;
import com.vanguard.assessment.utils.CSVGenerator;
import com.vanguard.assessment.utils.CSVParser;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

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
                return LocalDateTime.parse(value, DTF);
            return value;
        });

        results = results.stream().map(map -> {
            Map<String, Object> newMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if ("game_no".equals(entry.getKey())) {
                    newMap.put("gameNo", entry.getValue());
                } else if ("game_name".equals(entry.getKey())) {
                    newMap.put("gameName", entry.getValue());
                } else if ("game_code".equals(entry.getKey())) {
                    newMap.put("gameCode", entry.getValue());
                } else if ("cost_price".equals(entry.getKey())) {
                    newMap.put("costPrice", entry.getValue());
                } else if ("sale_price".equals(entry.getKey())) {
                    newMap.put("salePrice", entry.getValue());
                } else if ("date_of_sale".equals(entry.getKey())) {
                    newMap.put("dateOfSale", entry.getValue());
                } else {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }
            return newMap;
        }).toList();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        gameSalesRecords.addAll(results.stream().map(map -> objectMapper.convertValue(map, GameSales.class)).toList());
    }


    @Test
    public void testGetGameSales() {
        GameSalesCriteria criteria = new GameSalesCriteria();
        criteria.setFromDate(LocalDate.now());
        criteria.setToDate(LocalDate.now());
//        criteria.setFromSalePrice();
//        criteria.setToSalePrice();
//
//        gameSalesService.getGameSales();

    }

    @Test
    public void testGetGameSales2() {

//        gameSalesService.getTotalSales();

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
                            Mockito.mock(Pageable.class) ))
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
                        return new PageImpl<>(gameSales, invocation.getArgument(4), total);
                    });

            return mock;
        }

        @Bean
        GameDailySalesRepository gameDailySalesRepository() {

            GameDailySalesRepository mock = Mockito.mock(GameDailySalesRepository.class);
            Mockito.when(mock.findByGameNoAndDateBetweenOrderByDate(Mockito.anyInt(), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class) ))
                    .thenReturn(List.of());

            return mock;
        }

        @Bean
        GameMonthlySalesRepository gameMonthlySalesRepository() {

            GameMonthlySalesRepository mock = Mockito.mock(GameMonthlySalesRepository.class);
            Mockito.when(mock.findByGameNoAndDateBetweenOrderByDate(Mockito.anyInt(), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class) ))
                    .thenReturn(List.of());

            return mock;
        }

        @Bean
        TotalDailySalesRepository totalDailySalesRepository() {

            TotalDailySalesRepository mock = Mockito.mock(TotalDailySalesRepository.class);
            Mockito.when(mock.findByDateBetweenOrderByDate(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class) ))
                    .thenReturn(List.of());

            return mock;
        }

        @Bean
        TotalMonthlySalesRepository totalMonthlySalesRepository() {

            TotalMonthlySalesRepository mock = Mockito.mock(TotalMonthlySalesRepository.class);
            Mockito.when(mock.findByDateBetweenOrderByDate(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class) ))
                    .thenReturn(List.of());

            return mock;
        }
    }

}
