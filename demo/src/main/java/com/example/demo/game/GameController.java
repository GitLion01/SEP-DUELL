package com.example.demo.game;

import com.example.demo.game.requests.*;
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

    @MessageMapping("/attackCard")
    @SendTo("/topic/game")
    public Game attackCard(@Payload AttackRequest request) {
        return gameService.attackCard(request);
    }

    @MessageMapping("/attackUser")
    @SendTo("/topic/game")
    public Game attackUser(@Payload Long userA, Long userB) {
        return gameService.attackUser(userA, userB);
    }

    @MessageMapping("/endTurn")
    @SendTo("/topic/game")
    public Game endTurn(@Payload EndTurnRequest request) {
        return gameService.endTurn(request);
    }

    @MessageMapping("/rareSwap")
    @SendTo("/topic/game")
    public Game swapForRare(@Payload RareSwapRequest request){
        return gameService.swapForRare(request);
    }

    @MessageMapping("/LegendarySwap")
    @SendTo("/topic/game")
    public Game swapForLegendary(@Payload LegendarySwapRequest request){
        return gameService.swapForLegendary(request);
    }

    @MessageMapping("/doNothing")
    @SendTo("/topic/game")
    public Game doNothing(@Payload Long userID){
        return gameService.doNothing(userID);
    }

    @MessageMapping("/terminateMatch")
    @SendTo("/topic/game")
    public Game terminateMatch(@Payload Long gameID) {
        return gameService.terminateMatch(gameID);
    }


}
