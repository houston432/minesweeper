package ru.studiotg.minesweeper.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FieldState {
    CLOSED(" "),
    MINE("M"),
    EXPLODED_MINE("X");

    private final String value;

}
