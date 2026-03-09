package ru.studiotg.minesweeper.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.studiotg.minesweeper.exception.GameException;
import ru.studiotg.minesweeper.dto.GameInfoResponse;
import ru.studiotg.minesweeper.dto.GameTurnRequest;
import ru.studiotg.minesweeper.dto.NewGameRequest;
import ru.studiotg.minesweeper.service.GameService;
import ru.studiotg.minesweeper.type.FieldState;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameService gameService;

    private NewGameRequest validGameRequest;
    private GameTurnRequest validTurnRequest;
    private GameInfoResponse gameResponse;
    private ResponseEntity<GameInfoResponse> responseEntity;

    @BeforeEach
    void setUp() {
        validGameRequest = new NewGameRequest(10, 10, 10);
        validTurnRequest = new GameTurnRequest("test-game-id", 5, 5);

        String[][] field = new String[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                field[i][j] = FieldState.CLOSED.getValue();
            }
        }

        gameResponse = new GameInfoResponse(
                "test-game-id", 10, 10, 10, false, field
        );
        responseEntity = new ResponseEntity<>(gameResponse, HttpStatus.OK);
    }

    @Test
    void createGameWithValidRequest() throws Exception {
        when(gameService.createGame(any(NewGameRequest.class)))
                .thenReturn(responseEntity);

        mockMvc.perform(post("/api/games/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGameRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.game_id").value("test-game-id"))
                .andExpect(jsonPath("$.width").value(10))
                .andExpect(jsonPath("$.height").value(10))
                .andExpect(jsonPath("$.mines_count").value(10))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.field").isArray());
    }

    @Test
    void createGameWithInvalidRequest() throws Exception {
        NewGameRequest invalidRequest = new NewGameRequest(null, 10, 10);

        when(gameService.createGame(any(NewGameRequest.class)))
                .thenThrow(new GameException("Необходимо указать ширину поля."));

        mockMvc.perform(post("/api/games/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void makeTurnWithValidRequest() throws Exception {
        when(gameService.makeTurn(any(GameTurnRequest.class)))
                .thenReturn(responseEntity);

        mockMvc.perform(post("/api/games/turn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTurnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.game_id").value("test-game-id"));
    }

    @Test
    void makeTurnWithInvalidGameId() throws Exception {
        when(gameService.makeTurn(any(GameTurnRequest.class)))
                .thenThrow(new GameException("Игра с указанным game_id не найдена"));

        mockMvc.perform(post("/api/games/turn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTurnRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Игра с указанным game_id не найдена"));
    }

    @Test
    void makeTurnWithNullGameId() throws Exception {
        GameTurnRequest invalidRequest = new GameTurnRequest(null, 5, 5);

        mockMvc.perform(post("/api/games/turn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void makeTurnWithNegativeCoordinates() throws Exception {
        GameTurnRequest invalidRequest = new GameTurnRequest("test-id", -1, 5);

        mockMvc.perform(post("/api/games/turn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
