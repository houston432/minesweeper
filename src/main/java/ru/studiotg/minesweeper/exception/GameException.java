package ru.studiotg.minesweeper.exception;

import lombok.Getter;

@Getter
public class GameException extends RuntimeException {
    private final String message;

    public GameException(String message) {
        super(message);
        this.message = message;
    }
}
