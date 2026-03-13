package ru.studiotg.minesweeper.type;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum FieldState {
    CLOSED(" "),
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    MINE("M"),
    EXPLODED_MINE("X");

    private final String value;

    // Создаем статическую карту для быстрого поиска по числовым значениям
    private static final Map<Integer, FieldState> NUMBER_MAP = new HashMap<>();

    static {
        // Заполняем карту только для числовых значений (0-8)
        NUMBER_MAP.put(0, ZERO);
        NUMBER_MAP.put(1, ONE);
        NUMBER_MAP.put(2, TWO);
        NUMBER_MAP.put(3, THREE);
        NUMBER_MAP.put(4, FOUR);
        NUMBER_MAP.put(5, FIVE);
        NUMBER_MAP.put(6, SIX);
        NUMBER_MAP.put(7, SEVEN);
        NUMBER_MAP.put(8, EIGHT);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    // Метод для получения Enum по числовому значению
    public static FieldState fromNumber(int number) {
        FieldState state = NUMBER_MAP.get(number);
        if (state == null) {
            throw new IllegalArgumentException("Invalid number for field state: " + number);
        }
        return state;
    }

}
