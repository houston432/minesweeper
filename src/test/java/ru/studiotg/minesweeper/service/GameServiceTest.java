package ru.studiotg.minesweeper.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.studiotg.minesweeper.exception.GameException;
import ru.studiotg.minesweeper.dto.GameInfoResponse;
import ru.studiotg.minesweeper.dto.GameTurnRequest;
import ru.studiotg.minesweeper.dto.NewGameRequest;
import ru.studiotg.minesweeper.model.Game;
import ru.studiotg.minesweeper.type.FieldState;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @InjectMocks
    private GameService gameService;

    private NewGameRequest validGameRequest;
    private GameInfoResponse createdGame;

    @BeforeEach
    void setUp() {
        validGameRequest = new NewGameRequest(10, 10, 10);
    }

    @Test
    void createGameValidRequest() {
        ResponseEntity<GameInfoResponse> response = gameService.createGame(validGameRequest);

        assertNotNull(response);

        GameInfoResponse gameInfo = response.getBody();

        assertNotNull(gameInfo.gameId());
        assertEquals(10, gameInfo.width());
        assertEquals(10, gameInfo.height());
        assertEquals(10, gameInfo.minesCount());
        assertFalse(gameInfo.completed());
        assertNotNull(gameInfo.field());
        assertEquals(10, gameInfo.field().length);
        assertEquals(10, gameInfo.field()[0].length);

        // Проверяем, что все ячейки закрыты
        for (FieldState[] row : gameInfo.field()) {
            for (FieldState cell : row) {
                assertEquals(FieldState.CLOSED, cell);
            }
        }
    }

    @Test
    void createGamWithInvalidMinesCount() {
        NewGameRequest invalidRequest = new NewGameRequest(10, 10, 100); // max 99

        GameException exception = assertThrows(
                GameException.class,
                () -> gameService.createGame(invalidRequest)
        );

        assertTrue(exception.getMessage().contains("Количество мин"));
    }

    @Test
    void makeTurnWithValidMove() {
        ResponseEntity<GameInfoResponse> response = gameService.createGame(validGameRequest);
        createdGame = response.getBody();

        GameTurnRequest turnRequest = new GameTurnRequest(createdGame.gameId(), 5, 5);

        GameInfoResponse turnResponse = gameService.makeTurn(turnRequest).getBody();

        assertNotNull(response);
        assertEquals(createdGame.gameId(), turnResponse.gameId());
        assertFalse(turnResponse.completed());

        // Проверяем, что ячейка открылась
        assertNotEquals(FieldState.CLOSED.getValue(), turnResponse.field()[5][5]);
    }

    @Test
    void makeTurnOnMine() {

        createdGame = gameService.createGame(new NewGameRequest(2, 2, 1)).getBody();

        // Находим мину
        int[] mineRowCol = findMine(createdGame);

        GameTurnRequest turnRequest = new GameTurnRequest(createdGame.gameId(), mineRowCol[1], mineRowCol[0]);
        GameInfoResponse response = gameService.makeTurn(turnRequest).getBody();

        assertTrue(response.completed());

        // Проверяем, что мина отмечена как X
        boolean mineFound = FieldState.EXPLODED_MINE.equals(response.field()[mineRowCol[0]][mineRowCol[1]]);

        assertTrue(mineFound);
    }



    @Test
    void makeTurnOnAlreadyOpenedCell() {
        createdGame = gameService.createGame(validGameRequest).getBody();

        Game game = findGame(createdGame.gameId());
        boolean [][] mines = game.getMines();
        int safeRow = -1, safeCol = -1;
        for (int i = 0; i < mines.length; i++) {
            for (int j = 0; j < mines[i].length; j++) {
                if(!mines[i][j]) {
                    safeRow = i;
                    safeCol = j;
                }
                if(safeRow > -1){
                    break;
                }
            }
        }

        GameTurnRequest turnRequest = new GameTurnRequest(createdGame.gameId(), safeCol, safeRow);

        // Открываем ячейку первый раз
        gameService.makeTurn(turnRequest);

        GameException exception = assertThrows(
                GameException.class,
                () -> gameService.makeTurn(turnRequest)
        );

        assertEquals("Нельзя открыть уже открытую ячейку.", exception.getMessage());
    }

    @Test
    void makeTurnWithInvalidGameId() {
        GameTurnRequest turnRequest = new GameTurnRequest("invalid-id", 5, 5);

        GameException exception = assertThrows(
                GameException.class,
                () -> gameService.makeTurn(turnRequest)
        );

        assertTrue(exception.getMessage().contains("Игра с указанным game_id"));
    }

    @Test
    void makeTurnWithInvalidCoordinates() {
        createdGame = gameService.createGame(validGameRequest).getBody();
        GameTurnRequest turnRequest = new GameTurnRequest(createdGame.gameId(), 100, 100);

        GameException exception = assertThrows(
                GameException.class,
                () -> gameService.makeTurn(turnRequest)
        );

        assertTrue(exception.getMessage().contains("выходят за пределы"));
    }

    @Test
    void makeTurnAfterGameCompleted() {
        createdGame = gameService.createGame(validGameRequest).getBody();

        // Находим мину и подрываемся
        int[] mineRowCol = findMine(createdGame);

        GameTurnRequest turnMineRequest = new GameTurnRequest(createdGame.gameId(), mineRowCol[1], mineRowCol[0]);
        gameService.makeTurn(turnMineRequest).getBody();

        GameTurnRequest turnRequest = new GameTurnRequest(createdGame.gameId(), 5, 5);

        GameException exception = assertThrows(
                GameException.class,
                () -> gameService.makeTurn(turnRequest)
        );

        assertEquals("Игра уже завершена", exception.getMessage());
    }

    @Test
    void makeTurnThatWinsGame() {
        // Создаем игру, где можно быстро победить
        // Например, поле 2x2 с 1 миной
        createdGame = gameService.createGame(new NewGameRequest(2, 2, 1)).getBody();
        int[] mineRowCol = findMine(createdGame);
        int lastRow = -1, lastCol = -1;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if(i == mineRowCol[0] && j == mineRowCol[1]) {
                    continue;
                }
                if(lastRow == -1){
                    //Сохраняем безопасную ячейку, для открытия в конце
                    lastRow = i;
                    lastCol = j;
                } else {
                    // Открываем безопасные ячейки (2 штуки)
                    gameService.makeTurn(new GameTurnRequest(createdGame.gameId(), j, i));
                }
            }
        }

        // Открываем последнюю безопасную ячейку
        GameTurnRequest finalTurn = new GameTurnRequest(createdGame.gameId(), lastCol, lastRow);
        GameInfoResponse response = gameService.makeTurn(finalTurn).getBody();

        assertTrue(response.completed());

        // Проверяем, что мины отмечены как M
        int mineCount = 0;
        for (FieldState[] row : response.field()) {
            for (FieldState cell : row) {
                if (FieldState.MINE.equals(cell)) {
                    mineCount++;
                }
            }
        }
        assertEquals(1, mineCount);
    }

    @Test
    void openCellWithZeroNeighbours() {
        // Создаем поле с областью нулей

        createdGame = gameService.createGame(new NewGameRequest(5, 5, 5)).getBody();

        // Находим ячейку с 0 соседних мин
        int zeroRow = -1, zeroCol = -1;
        Game game = findGame(createdGame.gameId());
        int[][] neighbours = game.getNeighbourMines();
        for (int i = 0; i < neighbours.length; i++) {
            for (int j = 0; j < neighbours[i].length; j++) {
                if(neighbours[i][j]==0){
                    zeroRow = i;
                    zeroCol = j;
                    break;
                }
            }
            if(zeroRow > -1){
                break;
            }
        }

        GameTurnRequest turnRequest = new GameTurnRequest(createdGame.gameId(), zeroCol, zeroRow);
        GameInfoResponse response = gameService.makeTurn(turnRequest).getBody();

        // Проверяем, что открылась целая область
        int openedCount = countOpenedCells(response.field());

        assertTrue(openedCount > 1); // Открылось больше одной ячейки
    }

    private int countOpenedCells(FieldState[][] field) {
        int count = 0;
        for (FieldState[] row : field) {
            for (FieldState cell : row) {
                if (!FieldState.CLOSED.equals(cell)) {
                    count++;
                }
            }
        }
        return count;
    }

    // Метод для доступа к внутреннему хранилищу игр (рефлексия)
    private Game findGame(String gameId) {
        try {
            Field field = GameService.class.getDeclaredField("games");
            field.setAccessible(true);
            Map<String, Game> games = (Map<String, Game>) field.get(gameService);
            return games.get(gameId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access games storage", e);
        }
    }
    private int[] findMine(GameInfoResponse gameInfo) {
        int[] result = new int[2];
        Game game = findGame(gameInfo.gameId());
        for(int i = 0; i < game.getMines().length; i++){
            for (int j = 0; j < game.getMines()[i].length; j++){
                if(game.getMines()[i][j]){
                    result[0] = i;
                    result[1] = j;
                    break;
                }
            }
        }
        return result;
    }
}