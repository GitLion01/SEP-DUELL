package com.example.demo.game;
import com.example.demo.game.requests.*;
import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
                Iterator<UserAccount> gameIterator = game.getUsers().iterator();
                List<String> usernames = new ArrayList<>();
                while(gameIterator.hasNext()){
                    UserAccount user = gameIterator.next();
                    usernames.add(user.getUsername());
                }
                if(usernames.size() == 1) {
                    usernames.add("Bot");
                    streamedGames.put(game.getId(),usernames);
                }else{
                    streamedGames.put(game.getId(),usernames);
                }
            }

            return ResponseEntity.ok(streamedGames);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @MessageMapping("/streamGame")
    public void streamGame(@Payload StreamGameRequest request){
        gameService.streamGame(request);
    }

    @MessageMapping("/watchStream")
    public void watchStream(@Payload WatchStreamRequest request){
        gameService.watchStream(request);
    }

    @MessageMapping("/leaveStream")
    public void leaveStream(@Payload LeaveStreamRequest request){
        System.out.println("Leave Stream eingegangen");
        gameService.leaveStream(request);
        System.out.println("leaveStream ausgeführt");
    }


    @MessageMapping("/createGame")
    public void createGame(@Payload CreateGameRequest request) {
        gameService.createGame(request);
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

    @MessageMapping("/surrender")
    public void surrender(@Payload SurrenderRequest request){
        gameService.surrender(request);
    }

    @MessageMapping("/createBotGame")
    public void createBotGame(@Payload CreateBotRequest request) {
        System.out.println("AttackBotCard Controller Eingang");
        gameService.createBotGame(request);
        System.out.println("AttackBotCard Controller Ausgang");
    }

    @MessageMapping("/attackBotCard")
    public void attackBotCard(@Payload AttackBotCardRequest request){
        gameService.attackBotCard(request);
    }

    @MessageMapping("/attackBot")
    public void attackBot(@Payload AttackBotRequest request){
        System.out.println("Vor dem Angriff");
        gameService.attackBot(request);
        System.out.println("Nach dem Angriff");
    }

}
