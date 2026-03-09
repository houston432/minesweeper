package ru.studiotg.minesweeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record NewGameRequest(
        @NotNull(message = "Необходимо указать ширину поля.")
        @Positive(message = "Ширина поля должна быть больше 0.")
        @Max(value = 30, message = "Ширина поля должна быть меньше 31.")
        Integer width,
        @NotNull(message = "Необходимо указать высоту поля.")
        @Positive(message = "Высота поля должна быть больше 0.")
        @Max(value = 30, message = "Высота поля должна быть меньше 31.")
        Integer height,
        @JsonProperty("mines_count")
        @NotNull(message = "Необходимо указать количество мин.")
        @Positive(message = "Количество мин должно быть больше 0.")
        Integer minesCount){}
