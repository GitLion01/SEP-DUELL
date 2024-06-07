package com.example.demo.game;

import com.example.demo.cards.CardInstance;
import com.example.demo.cards.CardInstanceRepository;
import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.game.requests.*;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            return new ResponseEntity<>(game, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    public ResponseEntity<Game> playCard(PlayCardRequest request) {
       Optional<Game> optionalGame = gameRepository.findByidAndId(request.getGameID(), request.getUserID());
       Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(request.getUserID());



        return null;
    }


    public Game endTurn(EndTurnRequest request) {
        // Implementierung
        return null;
    }


    public Game attackCard(AttackRequest request) {
        // Implementierung
        return null;
    }


    public Game attackUser(Long userA, Long UserB) {
        // Implementierung
        return null;
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


    public Game terminateMatch(Long gameID) {
        // Implementierung
        return null;
    }


}
