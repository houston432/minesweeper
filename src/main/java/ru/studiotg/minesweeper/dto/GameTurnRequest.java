package ru.studiotg.minesweeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record GameTurnRequest(@JsonProperty("game_id")
                              @NotNull(message = "Необходимо передать game_id.")
                              String gameId,
                              @NotNull(message = "Необходимо передать номер столбца(col).")
                              @PositiveOrZero(message = "Номер столбца должен быть больше 0.")
                              Integer col,
                              @NotNull(message = "Необходимо передать номер строки(row).")
                              @PositiveOrZero(message = "Номер строки должен быть больше 0.")
                              Integer row){}
