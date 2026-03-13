package ru.studiotg.minesweeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.studiotg.minesweeper.type.FieldState;

public record GameInfoResponse (@JsonProperty("game_id") String gameId, int width, int height,
                                @JsonProperty("mines_count") int minesCount, boolean completed, FieldState[][] field){}
