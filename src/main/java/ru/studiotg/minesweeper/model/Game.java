package ru.studiotg.minesweeper.model;

import lombok.Data;
import ru.studiotg.minesweeper.type.FieldState;

import java.util.UUID;

@Data
public class Game {
    private String gameId;
    private int width;
    private int height;
    private int minesCount;
    private boolean completed;
    private boolean won;
    private FieldState[][] field;        // Текущее поле (что видит игрок)
    private boolean[][] mines;        // Расположение мин
    private boolean[][] opened;       // Открытые ячейки
    private int[][] neighbourMines;   // Количество мин рядом

    public Game(int width, int height, int minesCount) {
        this.gameId = UUID.randomUUID().toString();
        this.width = width;
        this.height = height;
        this.minesCount = minesCount;
        this.completed = false;
        this.won = false;
        this.field = new FieldState[height][width];
        this.mines = new boolean[height][width];
        this.opened = new boolean[height][width];
        this.neighbourMines = new int[height][width];

        // Инициализация поля пробелами
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                field[i][j] = FieldState.CLOSED;
            }
        }
    }
}
