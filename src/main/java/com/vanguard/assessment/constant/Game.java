package com.vanguard.assessment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static com.vanguard.assessment.constant.Constants.GameName;
import static com.vanguard.assessment.constant.Constants.GameCode;

@Getter
@AllArgsConstructor
public enum Game {

    ABC(1, GameName.ABC, GameCode.ABC, GameType.ONLINE, BigDecimal.valueOf(9.99)),
    DEF(2, GameName.DEF, GameCode.DEF, GameType.OFFLINE, BigDecimal.valueOf(49.99)),
    GHI(3, GameName.GHI, GameCode.GHI, GameType.OFFLINE, BigDecimal.valueOf(13.99)),
    ;
    private final int number;
    private final String name;
    private final String code;
    private final GameType type;
    private final BigDecimal costPrice;

    public static Optional<Game> fromGameNo(Integer gameNo) {
        return Arrays.stream(values())
                .filter(game -> Objects.equals(game.getNumber(), gameNo))
                .findFirst();
    }

    public static Optional<Game> fromGameCode(String gameCode) {
        return Arrays.stream(values())
                .filter(game -> Objects.equals(game.getCode(), gameCode))
                .findFirst();
    }
}
