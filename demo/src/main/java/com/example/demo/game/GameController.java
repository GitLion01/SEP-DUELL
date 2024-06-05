package com.example.demo.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/join")
    @SendTo("/topic/game")
    public Game joinGame(@Payload JoinRequest request) {
        return gameService.joinGame(request);
    }

    @MessageMapping("/selectDeck")
    @SendTo("/topic/game")
    public Game selectDeck(@Payload DeckSelectionRequest request) {
        return gameService.selectDeck(request);
    }

    @MessageMapping("/playCard")
    @SendTo("/topic/game")
    public Game playCard(@Payload PlayCardRequest request) {
        return gameService.playCard(request);
    }

    @MessageMapping("/attack")
    @SendTo("/topic/game")
    public Game attack(@Payload AttackRequest request) {
        return gameService.attack(request);
    }

    @MessageMapping("/endTurn")
    @SendTo("/topic/game")
    public Game endTurn(@Payload EndTurnRequest request) {
        return gameService.endTurn(request);
    }


}
