package com.vanguard.assessment.service.impl;

import com.vanguard.assessment.constant.CsvImportStatus;
import com.vanguard.assessment.dto.ImportCsvResult;
import com.vanguard.assessment.entity.CsvImport;
import com.vanguard.assessment.repository.CsvImportRepository;
import com.vanguard.assessment.service.GameDataService;
import com.vanguard.assessment.utils.DateTimeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.vanguard.assessment.constant.Constants.LOGGED_IN_USER;

@Service
public class GameDataServiceImpl implements GameDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameDataServiceImpl.class);

    private final String[] HEADERS = {
            "id", "game_no", "game_name", "game_code", "type",
            "cost_price", "tax", "sale_price", "date_of_sale"
    };

    private JdbcTemplate jdbcTemplate;

    private CsvImportRepository csvImportRepository;

    @Value("${app.config.async.thread-pool.number-of-threads:20}")
    private int numberOfThreads;

    private ExecutorService executor;

    @PostConstruct
    void init() {
        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    @PreDestroy
    void destroy() {
        executor.shutdown();
    }

    @Value("${app.config.csv-import.base-file-path:/}")
    private String csvBaseFilePath;

    @Value("${app.config.csv-import.insert-sql.value-size:80}")
    private int insertSqlValueSize;

    @Value("${app.config.csv-import.record-insertion.batch-size:40000}")
    private int recordInsertionBatchSize;

    private static final String INSERT_VALUE = "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_COLUMN = "INSERT INTO VANGUARD.GAME_SALES " +
            "(ID, GAME_NO, GAME_NAME, GAME_CODE, TYPE, COST_PRICE, TAX," +
            "SALE_PRICE, DATE_OF_SALE, CREATED_DATE, CREATED_BY, UPDATED_DATE, UPDATED_BY) " +
            "VALUES ";

    @Override
    public ImportCsvResult importCsv(InputStream csvInputStream) throws Exception {
        CsvImport csvImport = null;
        try {
            byte[] bytes = IOUtils.toByteArray(csvInputStream);
            csvImport = createCsvImport(new ByteArrayInputStream(bytes));
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .build();
            CSVParser records = csvFormat.parse(new InputStreamReader(new ByteArrayInputStream(bytes)));

            asyncUpdateCsvImport(csvImport, CsvImportStatus.SAVING);

            long total = 0;
            int i = 0;
            int j = 0;
            int k = 0;
            List<List<CSVRecord>> list = new ArrayList<>();
            List<CSVRecord> subList = new ArrayList<>();
            List<Future<?>> futureList = new ArrayList<>();

            StringBuilder sb = new StringBuilder();
            String insertSql = INSERT_COLUMN + (INSERT_VALUE + ",").repeat(insertSqlValueSize-1) + INSERT_VALUE;
            for (CSVRecord record : records) {
                total++;
                i++;
                j++;
                subList.add(record);
                if (j == insertSqlValueSize) {
                    list.add(new ArrayList<>(subList));
                    subList.clear();
                    j = 0;
                }
                if (i == recordInsertionBatchSize) {
                    futureList.add(asyncBatchInsert(new ArrayList<>(list), insertSql));
                    list.clear();
                    i = 0;
                    System.out.println("batchInsert " + ++k);
                }
            }
            if (!list.isEmpty()) {
                futureList.add(asyncBatchInsert(list, insertSql));
            }
            if (!subList.isEmpty()) {
                futureList.add(asyncSingleInsert(subList));
            }
            System.out.println("Awaiting");
            int l = 1;
            for (Future<?> future : futureList) {
                future.get();
                System.out.println("future " + (l++) + " done");
            }
            asyncUpdateCsvImport(csvImport, CsvImportStatus.COMPLETED);

            return ImportCsvResult.builder()
                    .data("Successfully imported " + total + " records from csv.")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Encountered error", e);
            if (Objects.nonNull(csvImport))
                asyncUpdateCsvImport(csvImport, CsvImportStatus.FAILED, ExceptionUtils.getStackTrace(e).substring(0, 500));
            return ImportCsvResult.builder()
                    .data("Encountered error while importing data from csv")
                    .build();
        }
    }

    protected CsvImport createCsvImport(InputStream is) throws IOException {
        String csvImportId = UUID.randomUUID().toString();
        String fullFilePath = csvBaseFilePath + csvImportId + ".csv";
        executor.submit(() -> {
            try {
                FileOutputStream fos = new FileOutputStream(fullFilePath);
                IOUtils.copy(is, fos);
            } catch (IOException e) {
                LOGGER.error("Error writing file " + fullFilePath, e);
            }
        });
        CsvImport csvImport = new CsvImport();
        csvImport.setId(csvImportId);
        csvImport.setFilepath(csvBaseFilePath + csvImportId + ".csv");
        csvImport.setStatus(CsvImportStatus.PARSING.getCode());
        Timestamp now = new Timestamp(System.currentTimeMillis());
        csvImport.setCreatedDate(now);
        csvImport.setCreatedBy(LOGGED_IN_USER);
        csvImport.setUpdatedDate(now);
        csvImport.setUpdatedBy(LOGGED_IN_USER);

        return csvImportRepository.save(csvImport);
    }

    protected void asyncUpdateCsvImport(CsvImport csvImport, CsvImportStatus status) throws IOException {
        asyncUpdateCsvImport(csvImport, status, null);
    }

    protected void asyncUpdateCsvImport(CsvImport csvImport, CsvImportStatus status, String error) throws IOException {
        csvImport.setStatus(status.getCode());
        csvImport.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
        csvImport.setUpdatedBy(LOGGED_IN_USER);
        csvImport.setError(error);
        executor.submit(() -> csvImportRepository.save(csvImport));
    }

    private static final ParameterizedPreparedStatementSetter<List<CSVRecord>> PREPARED_STATEMENT_SETTER = new ParameterizedPreparedStatementSetter<List<CSVRecord>>() {
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
                ps.setObject(i++, LocalDateTime.parse(dateOfSale, DateTimeUtils.DATE_TIME_FORMATTER));
                ps.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
                ps.setString(i++, LOGGED_IN_USER);
                ps.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
                ps.setString(i++, LOGGED_IN_USER);
            }
        }
    };

    private Future<?> asyncBatchInsert(List<List<CSVRecord>> records, String insertSql) {
       return executor.submit(() -> jdbcTemplate.batchUpdate(insertSql, records,
               recordInsertionBatchSize/insertSqlValueSize/2, PREPARED_STATEMENT_SETTER));
    }

    private Future<?> asyncSingleInsert(List<CSVRecord> records) {
        int size = records.size();
        String insertSQL;
        if (records.size() == 1) insertSQL = INSERT_COLUMN + INSERT_VALUE;
        else insertSQL = INSERT_COLUMN + ((INSERT_VALUE +",").repeat(size-1)) +INSERT_VALUE;
        return executor.submit(() -> jdbcTemplate.batchUpdate(insertSQL, List.of(records), 1, PREPARED_STATEMENT_SETTER));
    }


    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setCsvImportRepository(CsvImportRepository csvImportRepository) {
        this.csvImportRepository = csvImportRepository;
    }

}
