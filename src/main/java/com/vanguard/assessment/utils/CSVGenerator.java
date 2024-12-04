package com.vanguard.assessment.utils;

import com.vanguard.assessment.constant.Game;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.stream.LongStream;

public class CSVGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVGenerator.class);

    private static final String[] HEADERS = {
            "id", "game_no", "game_name", "game_code", "type",
            "cost_price", "tax", "sale_price", "date_of_sale"
    };

    private final static DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void generateCsv(long amount) throws IOException {
        ClassPathResource cpr = new ClassPathResource("sample.csv");

        FileWriter fw = new FileWriter(cpr.getFile().getAbsolutePath());
        StringWriter sw = new StringWriter();
        Random random = new Random();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        try (final CSVPrinter printer = new CSVPrinter(fw, csvFormat)) {
            LongStream.range(1, amount+1).forEachOrdered(id -> {
                Game[] games = Game.values();
                Game game = games[random.nextInt(games.length)];
                int gameNo = game.getNumber();
                String gameName = game.getName();
                String gameCode = game.getCode();
                Integer type = game.getType().getCode();
                BigDecimal costPrice = game.getCostPrice();
                String tax = "9%";
                BigDecimal salePrice = costPrice.multiply(BigDecimal.valueOf(1.09)).setScale(2, RoundingMode.HALF_UP);
                int day = random.nextInt(30) + 1;
                String dayStr = day < 10 ? "0" + day : day +"";
                int hour = random.nextInt(24);
                String hourStr = hour < 10 ? "0" +hour : String.valueOf(hour);
                int minute = random.nextInt(60);
                String minuteStr = minute < 10 ? "0" +minute : String.valueOf(minute);
                int second = random.nextInt(60);
                String secondStr = second < 10 ? "0" +second : String.valueOf(second);
                int miliseconds = random.nextInt(1000);
                String milisecondStr = miliseconds < 10 ? "00" + miliseconds :
                        miliseconds < 100 ? "0" + miliseconds : String.valueOf(miliseconds);
                String dateOfSale = String.format("2024-04-%sT%s:%s:%s.%sZ",
                        dayStr, hourStr, minuteStr, secondStr, milisecondStr);

                try {
                    printer.printRecord(id, gameNo, gameName, gameCode, type,
                            costPrice, tax, salePrice, dateOfSale);
                } catch (IOException e) {
                    LOGGER.error("Encountered error", e);
                }
            });
        }
    }

    public static void main(String[] args) {
//        long start = System.currentTimeMillis();
        try {
            CSVGenerator.generateCsv(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(System.currentTimeMillis() - start);
    }

}
