package com.example.demo.game;
import com.example.demo.game.requests.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@AllArgsConstructor
public class GameController {

    private final GameService gameService;


    @GetMapping(path = "/initialStreams")
    //Gibt spiele zurück wo stream auf true ist und Game auf ready
    public ResponseEntity<Map<Long, List<String>>> getStreamedGames(){
        Optional<List<Game>> optionalGames = gameService.getStreamedGames();
        if(optionalGames.isPresent()){
            List<Game> games = optionalGames.get();
            Map<Long, List<String>> streamedGames = new HashMap<>();

            for(Game game : games){
                streamedGames.put(game.getId(), List.of(game.getUsers().get(0).getUsername(), game.getUsers().get(1).getUsername()));
            }

            return ResponseEntity.ok(streamedGames);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

   /* @MessageMapping("/streams")
    public void getAllStreams(){
        gameService.getAllStreams();
    }*/

    @MessageMapping("/streamGame")
    public void streamGame(Long gameId){
        gameService.streamGame(gameId);
    }

    @MessageMapping("/watchStream")
    public void watchStream(Long gameId, Long userId){
        gameService.watchStream(gameId, userId);
    }


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
