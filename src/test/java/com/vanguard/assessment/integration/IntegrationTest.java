package com.vanguard.assessment.integration;

import com.vanguard.assessment.dto.GameSalesDTO;
import com.vanguard.assessment.dto.GameSalesQueryResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    public static void loadCsv()  {
        ClassPathResource cpr = new ClassPathResource("sample.csv");
//        CSV

    }

    @AfterAll
    public static void dumpCsv() {

    }

    @Test
    public void greetingShouldReturnDefaultMessage() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/",
                String.class)).contains("Hello, World");
    }

    @Test
    public void importCsv() throws Exception {
        ClassPathResource cpr = new ClassPathResource("sample.csv");
        FileSystemResource resource = new FileSystemResource(cpr.getFile());
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, new LinkedMultiValueMap<>());

        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/importCsv",
                requestEntity, String.class);

        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        assertThat(responseEntity.getBody()).contains("CSV imported successfully");
    }

    @Test
    public void testGetGameSales(){
        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port + "/getGameSales",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testGetGameSalesByDateRange(){
        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                                "/getGameSales?fromDate={fromDate}&toDate={toDate}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
    }

    @Test
    public void testGetGameSalesByPriceRange(){
        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                                "/getGameSales?fromPrice={fromPrice}&toPrice={toPrice}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
    }

//    @ParameterizedTest
    @Test
    public void testGetGameSalesByPriceRangeAndDateRange(){
        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                "/getGameSales?fromPrice={fromPrice}&toPrice={toPrice}&fromDate={fromDate}&toDate={toDate}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
    }

    @Test
    public void testGetTotalSalesByDateRange(){
        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                                "/getTotalSales?fromDate={fromDate}&toDate={toDate}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testGetTotalSalesByGameNoAndByDateRange(){
        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
                restTemplate.exchange("http://localhost:" + port +
                                "/getTotalSales?gameNo={gameNo}&fromDate={fromDate}&toDate={toDate}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

    }

//    @Test
//    public void testGetTotalSalesInGameNumberByDateRange(){
//        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity =
//                restTemplate.exchange("http://localhost:" + port +
//                                "/getGameSales",
//                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
//                });
//    }

//    @Test
//    public void testGetTotalSalesMonthly(){
//        ResponseEntity<AggregateGameSalesQueryResult<AggregatedGameSalesDTO>> responseEntity = restTemplate.exchange("http://localhost:" + port + "/getGameSales",
//                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
//                });
//
//        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
//        assertThat(responseEntity.getBody());
//    }

//    @Test
//    public void testGetTotalSales(){
//        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity = restTemplate.exchange("http://localhost:" + port + "/getGameSales",
//                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
//                });
//    }
//
//    @Test
//    public void testGetTotalSales(){
//        ResponseEntity<GameSalesQueryResult<GameSalesDTO>> responseEntity = restTemplate.exchange("http://localhost:" + port + "/getGameSales",
//                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
//                });
//    }

    public static void main(String[] args) {
       URI s = new DefaultUriBuilderFactory("").uriString("http://localhost:8080/getSales?code={code} ")
                .build("ABC");
       System.out.println(s.toString());
    }
}
