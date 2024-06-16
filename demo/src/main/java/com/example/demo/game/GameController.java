package com.example.demo.game;
import com.example.demo.game.requests.*;
import com.example.demo.user.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class GameController {

    private final GameService gameService;


    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


     // "/all/create" oder "/specific/create"
     // Mapped als "/app/private": Antworten werden an alle User gesendet, die diesen Endbunkt subscribed haben




    @MessageMapping("/createGame")
    public void createGame(@Payload CreateGameRequest request) {
        System.out.println("ANFRAGE IN CONTROLLER EINGETROFFEN");
        gameService.createGame(request);
        System.out.println("ANFRAGE BEARBEITET");
    }


    @MessageMapping("/selectDeck")
    public void selectDeck(@Payload DeckSelectionRequest request) {
        gameService.selectDeck(request);
    }

    @MessageMapping("/drawCard")
    public void drawCard(@Payload DrawCardRequest request) {
        gameService.drawCard(request);
    }

    @MessageMapping("/placeCard")
    public void placeCard(@Payload PlaceCardRequest request) {
        gameService.placeCard(request);
    }


    @MessageMapping("/attackCard")
    public void attackCard(@Payload AttackCardRequest request) {
        gameService.attackCard(request);
    }

    @MessageMapping("/attackUser")
    public void attackUser(@Payload AttackUserRequest request) {
        gameService.attackUser(request);
    }

    @MessageMapping("/endTurn")
    public void endTurn(@Payload EndTurnRequest request) {
        gameService.endTurn(request);
    }

    @MessageMapping("/rareSwap")
    public void swapForRare(@Payload RareSwapRequest request) {
        gameService.swapForRare(request);
    }

    @MessageMapping("/legendarySwap")
    public void swapForLegendary(@Payload LegendarySwapRequest request) {
        gameService.swapForLegendary(request);
    }


    @MessageMapping("/terminateMatch")
    public void terminateMatch(@Payload Long gameId, Long userA, Long userB) {
        gameService.terminateMatch(gameId, userA, userB);
    }
}
