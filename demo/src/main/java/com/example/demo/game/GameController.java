package com.example.demo.game;
import com.example.demo.game.requests.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@CrossOrigin
@RestController
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }


     // "/all/create" oder "/specific/create"
     // Mapped als "/app/private": Antworten werden an alle User gesendet, die diesen Endbunkt subscribed haben




    @MessageMapping("/createGame")
    public void createGame(@Payload CreateGameRequest request) {
        Optional<Game> game = gameService.createGame(request.getUserId());
        if (game.isPresent()) {
            messagingTemplate.convertAndSend("/all/gameCreated", game.get());
        } else {
            messagingTemplate.convertAndSendToUser(request.getUserId().toString(), "/specific/errors", "Could not create game");
        }
    }


    @MessageMapping("/selectDeck")
    public void selectDeck(@Payload DeckSelectionRequest request) {

    }

    @MessageMapping("/playCard")
    public void playCard(@Payload PlayCardRequest request) {

    }


    @MessageMapping("/attackCard")
    public void attackCard(@Payload AttackCardRequest request) {

    }

    @MessageMapping("/attackUser")
    public void attackUser(@Payload AttackUserRequest request) {

    }

    @MessageMapping("/endTurn")
    public void endTurn(@Payload EndTurnRequest request) {

    }

    @MessageMapping("/rareSwap")
    public void swapForRare(@Payload RareSwapRequest request) {

    }

    @MessageMapping("/LegendarySwap")
    public void swapForLegendary(@Payload LegendarySwapRequest request) {

    }

    @MessageMapping("/doNothing")
    public void doNothing(@Payload Long userID) {

    }

    @MessageMapping("/terminateMatch")
    public void terminateMatch(@Payload Long gameID) {

    }
}
