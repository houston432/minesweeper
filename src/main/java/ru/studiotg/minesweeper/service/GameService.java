package ru.studiotg.minesweeper.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.studiotg.minesweeper.model.Game;
import ru.studiotg.minesweeper.exception.GameException;
import ru.studiotg.minesweeper.dto.GameInfoResponse;
import ru.studiotg.minesweeper.dto.GameTurnRequest;
import ru.studiotg.minesweeper.dto.NewGameRequest;
import ru.studiotg.minesweeper.type.FieldState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private static final int[][] NEIGHBOURS = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};

    public ResponseEntity<GameInfoResponse> createGame(NewGameRequest request) {
        // Валидация входных параметров
        validateNewGame(request);

        // Создание игры
        Game game = new Game(request.width(), request.height(), request.minesCount());

        // Расстановка мин
        placeMines(game);

        // Подсчет соседних мин для каждой ячейки
        calculateNeighbourMines(game);

        // Сохранение игры
        games.put(game.getGameId(), game);

        return ResponseEntity.ok(convertToResponse(game));
    }

    public ResponseEntity<GameInfoResponse> makeTurn(GameTurnRequest request) {
        Game game = games.get(request.gameId());

        // Проверка существования игры
        if (game == null) {
            throw new GameException("Игра с указанным game_id" + request.gameId() + " не найдена.");
        }

        // Проверка, не завершена ли игра
        if (game.isCompleted()) {
            throw new GameException("Игра уже завершена");
        }

        // Валидация координат
        validateCoordinates(game, request.row(), request.col());

        // Проверка, не открыта ли уже ячейка
        if (game.getOpened()[request.row()][request.col()]) {
            throw new GameException("Нельзя открыть уже открытую ячейку.");
        }

        // Обработка хода
        processTurn(game, request.row(), request.col());

        // Проверка победы
        checkWinCondition(game);

        return ResponseEntity.ok(convertToResponse(game));
    }

    private void validateNewGame(NewGameRequest request) {
        int maxMines = request.width() * request.height() - 1;
        if (request.minesCount() <= 0 || request.minesCount() > maxMines) {
            throw new GameException("Количество мин должно быть от 1 до " + maxMines);
        }
    }

    private void validateCoordinates(Game game, int row, int col) {
        if (row >= game.getHeight() || col >= game.getWidth()) {
            throw new GameException("Координаты выходят за пределы поля");
        }
    }

    private void placeMines(Game game) {
        int minesPlaced = 0;
        int width = game.getWidth();
        int height = game.getHeight();

        while (minesPlaced < game.getMinesCount()) {
            int row = (int) (Math.random() * height);
            int col = (int) (Math.random() * width);

            if (!game.getMines()[row][col]) {
                game.getMines()[row][col] = true;
                minesPlaced++;
            }
        }
    }

    private void calculateNeighbourMines(Game game) {
        for (int height = 0; height < game.getHeight(); height++) {
            for (int width = 0; width < game.getWidth(); width++) {
                if (game.getMines()[height][width]) {
                    game.getNeighbourMines()[height][width] = -1; // Мина
                    continue;
                }

                int count = 0;
                for (int[] n : NEIGHBOURS) {
                    int nHeight = height + n[0];
                    int nWidth = width + n[1];
                    if (nHeight >= 0 && nHeight < game.getHeight() && nWidth >= 0 && nWidth < game.getWidth()
                            && game.getMines()[nHeight][nWidth]) {
                        count++;
                    }
                }
                game.getNeighbourMines()[height][width] = count;
            }
        }
    }

    private void processTurn(Game game, int row, int col) {
        // Проверка на мину
        if (game.getMines()[row][col]) {
            // Игрок подорвался
            game.setCompleted(true);
            game.setWon(false);
            revealAllCells(game, true); // Открыть все поле с X для мин
            return;
        }

        // Открытие ячейки
        openCell(game, row, col);
    }

    private void openCell(Game game, int row, int col) {
        if (row < 0 || row >= game.getHeight() || col < 0 || col >= game.getWidth()
                || game.getOpened()[row][col]) {
            return;
        }

        game.getOpened()[row][col] = true;

        if (game.getNeighbourMines()[row][col] == 0) {
            // Рекурсивное открытие соседних ячеек для пустых клеток
            for (int[] n : NEIGHBOURS) {
                openCell(game, row + n[0], col + n[1]);
            }
        }
    }

    private void checkWinCondition(Game game) {
        int totalCells = game.getWidth() * game.getHeight();
        int openedCells = 0;

        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                if (game.getOpened()[i][j]) {
                    openedCells++;
                }
            }
        }

        // Победа, если открыты все клетки без мин
        if (openedCells == totalCells - game.getMinesCount()) {
            game.setCompleted(true);
            game.setWon(true);
            revealAllCells(game, false); // Открыть все поле с M для мин
        }
    }

    private void revealAllCells(Game game, boolean lost) {
        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                if (game.getMines()[i][j]) {
                    game.getField()[i][j] = lost ? FieldState.EXPLODED_MINE.getValue() : FieldState.MINE.getValue();
                } else {
                    game.getField()[i][j] = String.valueOf(game.getNeighbourMines()[i][j]);
                }
            }
        }
    }

    private GameInfoResponse convertToResponse(Game game) {
        // Обновление поля перед отправкой
        updateField(game);

        return new GameInfoResponse(
                game.getGameId(),
                game.getWidth(),
                game.getHeight(),
                game.getMinesCount(),
                game.isCompleted(),
                game.getField()
        );
    }

    private void updateField(Game game) {
        // Если игра завершена, поле уже обновлено через revealAllCells
        if (game.isCompleted()) {
            return;
        }

        // Обновление поля на основе открытых ячеек
        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                if (game.getOpened()[i][j]) {
                    game.getField()[i][j] = String.valueOf(game.getNeighbourMines()[i][j]);
                } else {
                    game.getField()[i][j] = FieldState.CLOSED.getValue();
                }
            }
        }
    }
}
