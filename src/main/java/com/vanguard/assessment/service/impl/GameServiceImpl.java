package com.vanguard.assessment.service.impl;

import com.vanguard.assessment.entity.GameSales;
import com.vanguard.assessment.repository.GameSalesRepository;
import com.vanguard.assessment.service.GameService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

@Service
public class GameServiceImpl implements GameService {

    private final String[] HEADERS = {
            "id", "game_no", "game_name", "game_code", "type",
            "cost_price", "tax", "sale_price", "date_of_sale"
    };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Autowired
    private GameSalesRepository gameSalesRepository;

//    ExecutorService executor = Executors.newWorkStealingPool();

    ExecutorService executor = Executors.newFixedThreadPool(25);

    public void parse(InputStreamReader in) throws Exception {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .build();

        CSVParser records = csvFormat.parse(in);

        Future[] all = new Future[25];

        int i = 0;
        int j = 0;
        List<CSVRecord> subList = new ArrayList<>();
        for (CSVRecord record : records) {
          subList.add(record);
          i++;
            if (i % 40000 == 0) {
                List<CSVRecord> clones = new ArrayList<>(subList);
                all[j++] = executor.submit(() -> batchInsert(clones));
                subList.clear();
//                System.out.println("j " + j);
            }
        }

        int k = 1;
        for (Future future : all) {
            future.get();
//            System.out.println("future " + (k++) + " done");
        }
    }

    private static final String INSERT_VALUES = "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?),"
            .repeat(79) + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT = "INSERT INTO VANGUARD.GAME_SALES " +
            "(ID, GAME_NO, GAME_NAME, GAME_CODE, TYPE, COST_PRICE, TAX," +
            "SALE_PRICE, DATE_OF_SALE, CREATED_DATE, CREATED_BY, UPDATED_DATE, UPDATED_BY) " +
            "VALUES " + INSERT_VALUES;

    private static final ParameterizedPreparedStatementSetter<List<CSVRecord>> SSS = new ParameterizedPreparedStatementSetter<List<CSVRecord>>() {
        @Override
        public void setValues(PreparedStatement ps, List<CSVRecord> records) throws SQLException {
            int i = 1;
            for (CSVRecord record : records) {
                String id = record.get("id");
                String gameNo = record.get("game_no");
                String gameName = record.get("game_name");
                String gameCode = record.get("game_code");
                String type = record.get("type");
                String costPrice = record.get("cost_price");
                String tax = record.get("tax");
                String salePrice = record.get("sale_price");
                String dateOfSale = record.get("date_of_sale");

                BigDecimal numericTax = BigDecimal.valueOf(Double.parseDouble(tax.replace("%", "")))
                        .divide(BigDecimal.valueOf(100L));

                ps.setLong(i++, Long.parseLong(id));
                ps.setInt(i++, Integer.parseInt(gameNo));
                ps.setString(i++, gameName);
                ps.setString(i++, gameCode);
                ps.setInt(i++, Integer.parseInt(type));
                ps.setBigDecimal(i++, new BigDecimal(costPrice));
                ps.setBigDecimal(i++, numericTax);
                ps.setBigDecimal(i++, new BigDecimal(salePrice));
                ps.setObject(i++, LocalDateTime.parse(dateOfSale, DTF));
                ps.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
                ps.setString(i++, "CURRENT_USER");
                ps.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
                ps.setString(i++, "CURRENT_USER");
            }
        }
    };

    private void batchInsert(List<CSVRecord> records) {
//        System.out.println("inserting " );
        List<List<CSVRecord>> splitRecords = records.stream().reduce(new ArrayList<>(), new BiFunction<List<List<CSVRecord>>, CSVRecord, List<List<CSVRecord>>>() {
            int i = 0;
            List<CSVRecord> subList = new ArrayList<>();
            @Override
            public List<List<CSVRecord>> apply(List<List<CSVRecord>> objects, CSVRecord record) {
                subList.add(record);
                i++;
                if (i == 80) {
                    objects.add(new ArrayList<>(subList));
                    subList.clear();
                    i = 0;
                }
                return objects;
            }
        }, new BinaryOperator<>() {

            @Override
            public List<List<CSVRecord>> apply(List<List<CSVRecord>> record, List<List<CSVRecord>> record2) {
                record.addAll(record2);
                return record;
            }
        });
//        System.out.println("splitRecords " + splitRecords.size());
//        splitRecords.forEach(subList -> System.out.println("subList " + subList.size()));
        try {
            jdbcTemplate.batchUpdate(INSERT, splitRecords, 500, SSS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(CSVRecord record) {
        String id = record.get("id");
        String gameNo = record.get("game_no");
        String gameName = record.get("game_name");
        String gameCode = record.get("game_code");
        String type = record.get("type");
        String costPrice = record.get("cost_price");
        String tax = record.get("tax");
        String salePrice = record.get("sale_price");
        String dateOfSale = record.get("date_of_sale");

        BigDecimal numericTax = BigDecimal.valueOf(Double.parseDouble(tax.replace("%", "")))
                .divide(BigDecimal.valueOf(100L));

        GameSales gameSales = new GameSales();
        gameSales.setId(Long.parseLong(id));
        gameSales.setGameNo(Integer.parseInt(gameNo));
        gameSales.setGameName(gameName);
        gameSales.setGameCode(gameCode);
        gameSales.setType(Integer.parseInt(type));
        gameSales.setCostPrice(BigDecimal.valueOf(Double.parseDouble(costPrice)));
        gameSales.setTax(numericTax);
        gameSales.setSalePrice(BigDecimal.valueOf(Double.parseDouble(salePrice)));
        gameSales.setDateOfSale(LocalDateTime.parse(dateOfSale, DTF));

        gameSalesRepository.save(gameSales);
    }


}
