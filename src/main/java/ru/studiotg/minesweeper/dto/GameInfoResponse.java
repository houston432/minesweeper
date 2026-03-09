package ru.studiotg.minesweeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameInfoResponse (@JsonProperty("game_id") String gameId, int width, int height,
                                @JsonProperty("mines_count") int minesCount, boolean completed, String[][] field){}
