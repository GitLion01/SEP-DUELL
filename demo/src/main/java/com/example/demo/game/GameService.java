package com.example.demo.game;
import com.example.demo.cards.CardInstance;
import com.example.demo.cards.CardInstanceRepository;
import com.example.demo.cards.Rarity;
import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.game.requests.*;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

   private final GameRepository gameRepository;
   private final PlayerStateRepository playerStateRepository;
   private final DeckRepository deckRepository;
   private final UserAccountRepository userAccountRepository;
   private final CardInstanceRepository cardInstanceRepository;

   @Autowired
   public GameService(GameRepository gameRepository,
                      PlayerStateRepository playerStateRepository,
                      DeckRepository deckRepository, UserAccountRepository userAccountRepository, CardInstanceRepository cardInstanceRepository) {
       this.gameRepository = gameRepository;
       this.deckRepository = deckRepository;
       this.playerStateRepository = playerStateRepository;
       this.userAccountRepository = userAccountRepository;
       this.cardInstanceRepository = cardInstanceRepository;
   }


   public Game joinGame(JoinRequest request) {
       // Implementierung
       return null;
   }

   //TODO: Statistik implementieren und bei den methoden die Daten in die Statistik-Tabelle speichern


   public ResponseEntity<Game> selectDeck(DeckSelectionRequest request) {

       Optional<Game> optionalGame = gameRepository.findByidAndId(request.getGameID(), request.getUserID());
       Optional<Deck> optionalDeck = deckRepository.findByNameAndUserId(request.getDeckName(), request.getUserID());
       Optional<PlayerState> optionalPlayerState = playerStateRepository.findByUserId(request.getUserID());

       if(optionalGame.isPresent() && optionalDeck.isPresent() && optionalPlayerState.isPresent()) {
           Game game = optionalGame.get();
           Deck deck = optionalDeck.get();
           PlayerState playerState = optionalPlayerState.get();

           playerState.setDeck(deck);
           playerStateRepository.save(playerState);
           gameRepository.save(game);
           return new ResponseEntity<>(game, HttpStatus.OK);
       }

       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
   }


   public ResponseEntity<Game> playCard(PlayCardRequest request) {
       Optional<Game> optionalGame = gameRepository.findByidAndId(request.getGameID(), request.getUserID());
       Optional<CardInstance> optionalCardInstance = cardInstanceRepository.findById(request.getCardInstanceID());
       Optional<PlayerState> optionalPlayerState = playerStateRepository.findByUserId(request.getUserID());

       if(optionalGame.isPresent() && optionalCardInstance.isPresent() && optionalPlayerState.isPresent()) {
           Game game = optionalGame.get();
           CardInstance cardInstance = optionalCardInstance.get();
           PlayerState playerState = optionalPlayerState.get();

           Rarity rarity = cardInstance.getCard().getRarity();
           if(playerState.getHand().contains(cardInstance) && rarity.toString().equals("NORMAL")){
               playerState.getField().add(cardInstance);
               playerState.getHand().remove(cardInstance);
               playerState.getCardsPlayed().add(cardInstance);
               playerStateRepository.save(playerState);
               game.setCurrentTurn(game.getCurrentTurn() == 0 ? 1 : 0);
               gameRepository.save(game);
           }
           return new ResponseEntity<>(game, HttpStatus.OK);
       }
       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
   }


   public ResponseEntity<Game> endTurn(EndTurnRequest request) {
       Optional<Game> optionalGame = gameRepository.findByidAndId(request.getGameID(), request.getUserID());

       if(optionalGame.isPresent()) {
           Game game = optionalGame.get();
           game.setCurrentTurn(game.getCurrentTurn() == 0 ? 1 : 0);
           gameRepository.save(game);
           return new ResponseEntity<>(game, HttpStatus.OK);
       }

       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
   }




   public ResponseEntity<Game> attackCard(AttackRequest request) {
       Optional<Game> optionalGame = gameRepository.findByidAndId(request.getGameID(), request.getUserIDAttacker());
       Optional<UserAccount> optionalAttackingUser = userAccountRepository.findById(request.getAttackerID());
       Optional<UserAccount> optionalDefendingUser = userAccountRepository.findById(request.getUserIDDefender());
       Optional<CardInstance> optionalAttacker = cardInstanceRepository.findById(request.getAttackerID());
       Optional<CardInstance> optionalTarget = cardInstanceRepository.findById(request.getTargetID());

       if(optionalGame.isPresent()
               && optionalAttacker.isPresent()
               && optionalTarget.isPresent()
               && optionalAttackingUser.isPresent()
               && optionalDefendingUser.isPresent()) {
           Game game = optionalGame.get();
           UserAccount attackingUser = optionalAttackingUser.get();
           UserAccount defendingUser = optionalDefendingUser.get();
           CardInstance attacker = optionalAttacker.get();
           CardInstance target = optionalTarget.get();

           if(game.getPlayers().contains(attackingUser)
                   && game.getPlayers().contains(defendingUser)
                   && game.isUsersTurn(attackingUser)
                   && attacker.getPlayerState().getField().contains(attacker)) {

               List<CardInstance> defenderCards = defendingUser.getPlayerState().getField();
               if(defenderCards.contains(target)) {
                   if(attacker.getCard().getAttackPoints() > target.getCard().getDefensePoints()){
                       defenderCards.remove(target);
                       defendingUser.getPlayerState().getField().remove(target);
                       userAccountRepository.save(defendingUser);
                       game.setCurrentTurn(game.getCurrentTurn() == 0 ? 1 : 0);
                   }else{
                       target.getCard().setDefensePoints(target.getCard().getDefensePoints() - attacker.getCard().getDefensePoints());
                       attackingUser.getPlayerState().setDamage(attackingUser.getPlayerState().getDamage() + attacker.getCard().getDefensePoints());
                       userAccountRepository.save(attackingUser);
                       game.setCurrentTurn(game.getCurrentTurn() == 0 ? 1 : 0);
                   }
                   gameRepository.save(game);
               }
           }
           return new ResponseEntity<>(game, HttpStatus.OK);
       }
       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
   }




   public ResponseEntity<Game> attackUser(Long attackerID, Long defenderID, Long gameID, Long attackerCardID) {
       Optional<UserAccount> optionalAttacker = userAccountRepository.findById(attackerID);
       Optional<UserAccount> optionalDefender = userAccountRepository.findById(defenderID);
       Optional<Game> optionalGame = gameRepository.findById(gameID);
       Optional<CardInstance> optionalCard = cardInstanceRepository.findById(attackerCardID);

       if(optionalGame.isPresent()
               && optionalAttacker.isPresent()
               && optionalDefender.isPresent()
               && optionalCard.isPresent()){

           Game game = optionalGame.get();
           CardInstance card = optionalCard.get();
           UserAccount attacker = optionalAttacker.get();
           UserAccount defender = optionalDefender.get();

           if(game.getPlayers().contains(attacker)
                   && game.getPlayers().contains(defender)
                   && game.isUsersTurn(attacker)
                   && attacker.getPlayerState().getField().contains(card)
                   && defender.getPlayerState().getField().isEmpty()){

               if(card.getCard().getAttackPoints() > defender.getPlayerState().getLifePoints()){

                   attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + card.getCard().getAttackPoints());
                   attacker.getPlayerState().resetPlayerstate();
                   defender.getPlayerState().resetPlayerstate();
                   GameService.terminateMatch(gameID);
                   userAccountRepository.save(attacker);
                   userAccountRepository.save(defender);
               }else{
                   defender.getPlayerState().setLifePoints(defender.getPlayerState().getLifePoints() - card.getCard().getAttackPoints());
                   game.setCurrentTurn(game.getCurrentTurn() == 0 ? 1 : 0);
                   gameRepository.save(game);
               }
           }

           return new ResponseEntity<>(game, HttpStatus.OK);
       }
       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
   }


   public Game swapForRare(RareSwapRequest request) {
       // Implementierung
       return null;
   }


   public Game swapForLegendary(LegendarySwapRequest request) {
       // Implementierung
       return null;
   }


   public Game doNothing(Long userID) {
       // Implementierung
       return null;
   }


   public static void terminateMatch(Long gameID) {
       // Implementierung
   }

}
