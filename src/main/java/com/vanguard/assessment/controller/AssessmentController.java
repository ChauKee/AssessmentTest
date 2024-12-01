package com.vanguard.assessment.controller;

import com.vanguard.assessment.constant.Error;
import com.vanguard.assessment.dto.GameSalesCriteria;
import com.vanguard.assessment.dto.GameSalesQueryResult;
import com.vanguard.assessment.service.GameSalesService;
import com.vanguard.assessment.service.GameService;

import com.vanguard.assessment.utils.DateTimeUtils;
import com.vanguard.assessment.utils.ValidationUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.NumberUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static com.vanguard.assessment.utils.DateTimeUtils.DATE_FORMATTER;

@RestController
public class AssessmentController {

    private GameService gameService;
    private GameSalesService gameSalesService;

    @Value("${app.config.pagination.page-size:100}")
    private int pageSize;

    @Autowired
    public AssessmentController(GameService gameService, GameSalesService gameSalesService) {
        this.gameService = gameService;
        this.gameSalesService = gameSalesService;
    }

    @GetMapping("test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World");
    }

    @PostMapping("import")
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) throws Exception {
        gameService.parse(new InputStreamReader(file.getInputStream()));
        return ResponseEntity.ok("This is the assessment");
    }

    @GetMapping("getGameSales")
    public ResponseEntity<?> getGameSales(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String toSalePrice,
            @RequestParam(required = false) String fromSalePrice,
            @RequestParam(required = false) String gameNo) {
        StringBuilder sb = new StringBuilder();
        if (Objects.nonNull(page) && !ValidationUtils.isValidIntegerRange(page, 1, Integer.MAX_VALUE)) {
            sb.append("page must be digit and equal/larger than 1,");
        }
        if (Objects.nonNull(fromDate) && !ValidationUtils.isValidDateFormat(fromDate, DATE_FORMATTER)) {
            sb.append("fromDate must be in yyyy-MM-dd format,");
        }
        if (Objects.nonNull(toDate) && !ValidationUtils.isValidDateFormat(fromDate, DATE_FORMATTER)) {
            sb.append("toDate must be in yyyy-MM-dd format,");
        }
        if (Objects.nonNull(toDate) && !ValidationUtils.isValidDateFormat(fromDate, DATE_FORMATTER)) {
            sb.append("toDate must be in yyyy-MM-dd format,");
        }
        if (Objects.nonNull(fromSalePrice) && !ValidationUtils.isValidDecimalRange(fromSalePrice, BigDecimal.ZERO)) {
            sb.append("fromSalePrice must be valid decimal and equal/larger than 0,");
        }
        if (Objects.nonNull(toSalePrice) && !ValidationUtils.isValidDecimalRange(toSalePrice, BigDecimal.ZERO)) {
            sb.append("toSalePrice must be valid decimal and equal/larger than 0,");
        }
        if (Objects.nonNull(gameNo) && !ValidationUtils.isValidIntegerRange(gameNo, 0, 100)) {
            sb.append("gameNo must be digit equal/larger than 0 and equal/smaller than 100,");
        }
        if (!sb.isEmpty()) {
            GameSalesQueryResult<?> badResult = GameSalesQueryResult.builder()
                    .error(Error.VALIDATION_ERROR.getDescription())
                    .errorMessage(sb.deleteCharAt(sb.length()-1).toString())
                    .build();
            return ResponseEntity.badRequest().body(badResult);
        }

        GameSalesCriteria criteria = new GameSalesCriteria();
        if (Objects.nonNull(fromSalePrice))
            criteria.setFromSalePrice(BigDecimal.valueOf(Double.parseDouble(fromSalePrice)));
        if (Objects.nonNull(toSalePrice))
            criteria.setToSalePrice(BigDecimal.valueOf(Double.parseDouble(toSalePrice)));
        if (Objects.nonNull(fromDate))
            criteria.setFromDate(LocalDate.parse(fromDate, DATE_FORMATTER));
        if (Objects.nonNull(toDate))
            criteria.setToDate(LocalDate.parse(toDate, DATE_FORMATTER));
        if (Objects.nonNull(gameNo))
            criteria.setGameNo(Integer.parseInt(gameNo));

        Pageable pageable = PageRequest.of(page == null ? 0 : Integer.parseInt(page) - 1, pageSize);

        return ResponseEntity.ok(gameSalesService.getGameSales(criteria, pageable));
    }

    @GetMapping("getTotalSales")
    public ResponseEntity<?> getTotalSales(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(required = false) String gameNo,
            @RequestParam String frequency) {
        StringBuilder sb = new StringBuilder();
        if (Objects.nonNull(fromDate) && !ValidationUtils.isValidDateFormat(fromDate, DATE_FORMATTER)) {
            sb.append("fromDate must be in yyyy-MM-dd format,");
        }
        if (Objects.nonNull(toDate) && !ValidationUtils.isValidDateFormat(toDate, DATE_FORMATTER)) {
            sb.append("toDate must be in yyyy-MM-dd format,");
        }
        if (Objects.nonNull(gameNo) && !ValidationUtils.isValidIntegerRange(gameNo, 0, 100)) {
            sb.append("gameNo must be digit and equal/larger than 0 and equal/smaller than 100,");
        }
        if (Objects.nonNull(frequency) && !ValidationUtils.isValidIntegerRange(frequency, 1, 2)) {
            sb.append("frequency must be digit and equal/larger than 1 and equal/smaller than 2,");
        }
        if (!sb.isEmpty()) {
            GameSalesQueryResult<?> badResult = GameSalesQueryResult.builder()
                    .error(Error.VALIDATION_ERROR.getDescription())
                    .errorMessage(sb.deleteCharAt(sb.length()-1).toString())
                    .build();
            return ResponseEntity.badRequest().body(badResult);
        }

        GameSalesCriteria criteria = new GameSalesCriteria();
        if (Objects.nonNull(fromDate))
            criteria.setFromDate(LocalDate.parse(fromDate, DATE_FORMATTER));
        if (Objects.nonNull(toDate))
            criteria.setToDate(LocalDate.parse(toDate, DATE_FORMATTER));
        if (Objects.nonNull(gameNo))
            criteria.setGameNo(Integer.parseInt(gameNo));
        criteria.setFrequency(frequency == null ? 1 : Integer.parseInt(frequency));

        System.out.printf("from=%s, toDate=%s, gameNo=%s, frequency=%s%n", fromDate, toDate, gameNo, frequency);

        return ResponseEntity.ok(gameSalesService.getTotalSales(criteria));
    }

}
