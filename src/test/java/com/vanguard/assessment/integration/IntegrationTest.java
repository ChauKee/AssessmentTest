package com.vanguard.assessment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vanguard.assessment.AssessmentApplication;
import com.vanguard.assessment.dto.*;
import com.vanguard.assessment.entity.GameSales;
import com.vanguard.assessment.utils.CSVParser;
import com.vanguard.assessment.utils.DateTimeUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AssessmentApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.endpoint.game.import-csv}")
    private String importEndpoint;

    @Value("${app.endpoint.game.getGameSales}")
    private String getGameSalesEndpoint;

    @Value("${app.endpoint.game.getTotalSales}")
    private String getTotalSalesEndpoint;

    private List<GameSales> gameSales;

    @BeforeAll
    public void init() throws Exception  {
        ClassPathResource cpr = new ClassPathResource("test_game_sales.csv");
        List<Map<String, Object>> results = CSVParser.parse(cpr.getInputStream(), CSVParser.DEFAULT_CSV_HEADERS, CSVParser.DEFAULT_CONVERSION);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        gameSales = results.stream().map(map -> objectMapper.convertValue(map, GameSales.class)).toList();
        jdbcTemplate.execute("TRUNCATE TABLE VANGUARD.GAME_SALES");
        jdbcTemplate.execute("TRUNCATE TABLE VANGUARD.CSV_IMPORT");

        // Note that the test_game_sales.csv has 1000 records, and set the app.config.pagination.page-size=1000
        // for integration tests
    }

    @AfterEach
    public void dumpCsv() {
        jdbcTemplate.execute("TRUNCATE TABLE VANGUARD.GAME_SALES");
        jdbcTemplate.execute("TRUNCATE TABLE VANGUARD.CSV_IMPORT");
    }

    @Test
    public void importCsv() throws Exception {
        ClassPathResource cpr = new ClassPathResource("test_game_sales.csv");
        FileSystemResource resource = new FileSystemResource(cpr.getFile());
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, new LinkedMultiValueMap<>());

        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/" + importEndpoint,
                requestEntity, String.class);

        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Long resultSet = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM VANGUARD.GAME_SALES", Long.class);
        assertEquals(resultSet, gameSales.size());

    }

    @Test
    public void testGetGameSalesByDateRange() throws Exception{
        importCsv();
        String fromDateStr = "2024-04-01";
        String toDateStr = "2024-04-30";
        Map<String, String> params = new HashMap<>();
        params.put("fromDate", fromDateStr);
        params.put("toDate", toDateStr);
        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                                "/" + getGameSalesEndpoint + "?fromDate={fromDate}&toDate={toDate}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, params);

        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        List<GameSalesDTO> results = responseEntity.getBody().getData();

        LocalDate fromDate = LocalDate.parse(fromDateStr, DateTimeUtils.DATE_FORMATTER);
        LocalDate toDate = LocalDate.parse(toDateStr, DateTimeUtils.DATE_FORMATTER);
        List<GameSales> expected = gameSales.stream()
                .filter(gs -> !gs.getDateOfSale().isBefore(fromDate.atStartOfDay()) &&
                        !gs.getDateOfSale().isAfter(toDate.plusDays(1).atStartOfDay()))
                .toList();
        long expectedTotal = expected.size();
        assertEquals( expectedTotal, results.size());

        assertTrue(results.stream().allMatch(result -> expected.stream().anyMatch(gs -> Objects.equals(result.getId(), gs.getId()))));
    }

    @Test
    public void testGetGameSalesByPriceRangeAndDateRange() throws Exception {
        importCsv();
        String fromDateStr = "2024-04-01";
        String toDateStr = "2024-04-30";
        String fromSalePriceStr = "0";
        String toSalePriceStr = "30";
        Map<String, String> params = new HashMap<>();
        params.put("fromDate", fromDateStr);
        params.put("toDate", toDateStr);
        params.put("fromSalePrice", fromSalePriceStr);
        params.put("toSalePrice", toSalePriceStr);

        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                                "/" + getGameSalesEndpoint + "?fromDate={fromDate}&toDate={toDate}&fromSalePrice={fromSalePrice}&toSalePrice={toSalePrice}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, params);


        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        List<GameSalesDTO> results = responseEntity.getBody().getData();

        LocalDate fromDate = LocalDate.parse(fromDateStr, DateTimeUtils.DATE_FORMATTER);
        LocalDate toDate = LocalDate.parse(toDateStr, DateTimeUtils.DATE_FORMATTER);
        BigDecimal fromSalePrice = new BigDecimal(fromSalePriceStr);
        BigDecimal toSalePrice = new BigDecimal(toSalePriceStr);
        List<GameSales> expected = gameSales.stream()
                .filter(gs -> !gs.getDateOfSale().isBefore(fromDate.atStartOfDay()) &&
                        !gs.getDateOfSale().isAfter(toDate.plusDays(1).atStartOfDay()))
                .filter(gs -> gs.getSalePrice().compareTo(fromSalePrice) != -1 &&
                        gs.getSalePrice().compareTo(toSalePrice) != 1)
                .toList();
        long expectedTotal = expected.size();
        assertEquals( expectedTotal, results.size());

        assertTrue(results.stream().allMatch(result -> expected.stream().anyMatch(gs -> Objects.equals(result.getId(), gs.getId()))));
    }

    @Test
    public void testGetTotalSalesByDateRange() throws Exception {
        importCsv();
        String fromDateStr = "2024-04-01";
        String toDateStr = "2024-04-30";
        Map<String, String> params = new HashMap<>();
        params.put("fromDate", fromDateStr);
        params.put("toDate", toDateStr);
        ResponseEntity<AggregateGameSalesQueryResult<AggregatedGameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                                "/" + getTotalSalesEndpoint + "?fromDate={fromDate}&toDate={toDate}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, params);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testGetTotalSalesByGameNoAndByDateRange() throws Exception {
        importCsv();
        String fromDateStr = "2024-04-01";
        String toDateStr = "2024-04-30";
        Integer gameNo = 1;
        Map<String, Object> params = new HashMap<>();
        params.put("fromDate", fromDateStr);
        params.put("toDate", toDateStr);
        params.put("gameNo", gameNo);
        ResponseEntity<AggregateGameSalesQueryResult<AggregatedGameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                                "/" + getTotalSalesEndpoint + "?gameNo={gameNo}&fromDate={fromDate}&toDate={toDate}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, params);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

    }

}
