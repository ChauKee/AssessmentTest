package com.vanguard.assessment.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vanguard.assessment.entity.GameSales;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import org.apache.commons.text.CaseUtils;

public class CSVParser {

    public static final String[] DEFAULT_CSV_HEADERS = {
            "id", "game_no", "game_name", "game_code", "type",
            "cost_price", "tax", "sale_price", "date_of_sale"
    };

    public static final BiFunction<String, String, Object> DEFAULT_CONVERSION  = (header, value) -> {
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
    };


    public static List<Map<String, Object>> parse(InputStream in, String[] headers, BiFunction<String, String, Object> conversion) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .setSkipHeaderRecord(true)
                .build();

        org.apache.commons.csv.CSVParser records = csvFormat.parse(new InputStreamReader(in));

        Map<String, Object> values;
        List<Map<String, Object>> results = new ArrayList<>();
        for (CSVRecord record : records) {
            values = new HashMap<>();
            for(String header : headers) {
                String camelCaseHeader = CaseUtils.toCamelCase(header,false, '_');
                String value = record.get(header);
                values.put(camelCaseHeader, conversion.apply(header, value));
            }
            results.add(values);
        }
        return results;

    }

    public static void main(String[] args) throws Exception {
        ClassPathResource cpr = new ClassPathResource("test_game_sales.csv");

        List<Map<String, Object>> results = CSVParser.parse(cpr.getInputStream(), DEFAULT_CSV_HEADERS, (header, value) -> {
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
        List<GameSales> gameSales = results.stream().map(map -> objectMapper.convertValue(map, GameSales.class)).toList();

//        System.out.println(objectMapper.writeValueAsString(gameSales));

    }

}
