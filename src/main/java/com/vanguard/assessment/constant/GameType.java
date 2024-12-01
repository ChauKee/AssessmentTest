package com.vanguard.assessment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum GameType {
    ONLINE(1, "online"),
    OFFLINE(2, "offline");

    private final Integer code;
    private final String description;

    public static Optional<GameType> fromCode(Integer code) {
        return Arrays.stream(values()).filter(gameType ->
                Objects.equals(gameType.getCode(), code)).findFirst();
    }
}
