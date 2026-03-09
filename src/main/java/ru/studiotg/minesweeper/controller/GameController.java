package ru.studiotg.minesweeper.controller;


import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.studiotg.minesweeper.dto.GameInfoResponse;
import ru.studiotg.minesweeper.dto.GameTurnRequest;
import ru.studiotg.minesweeper.dto.NewGameRequest;
import ru.studiotg.minesweeper.service.GameService;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/new")
    public ResponseEntity<GameInfoResponse> createGame(@Valid @RequestBody NewGameRequest request) {
        return gameService.createGame(request);
    }

    @PostMapping("/turn")
    public ResponseEntity<GameInfoResponse> makeTurn(@Valid @RequestBody GameTurnRequest request) {
        return gameService.makeTurn(request);
    }
}
