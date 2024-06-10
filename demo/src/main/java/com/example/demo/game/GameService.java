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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;

@Transactional
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
                       DeckRepository deckRepository,
                       UserAccountRepository userAccountRepository,
                       CardInstanceRepository cardInstanceRepository) {
        this.gameRepository = gameRepository;
        this.playerStateRepository = playerStateRepository;
        this.deckRepository = deckRepository;
        this.userAccountRepository = userAccountRepository;
        this.cardInstanceRepository = cardInstanceRepository;
    }



    public GameWithUsersDTO createGame(CreateGameRequest request) {
        System.out.println("Creating game for users A:" + request.getUserA() + " and B:" + request.getUserB());
        UserAccount userA = userAccountRepository.findById(request.getUserA())
                .orElseThrow(() -> new IllegalArgumentException("User A not found"));
        UserAccount userB = userAccountRepository.findByUsername(request.getUserB())
                .orElseThrow(() -> new IllegalArgumentException("User B not found"));

        // Check if userA is already associated with a game
        if (gameRepository.existsByUsersContaining(userA)) {
            throw new IllegalStateException("User A is already in a game");
        }

        // Check if userB is already associated with a game
        if (gameRepository.existsByUsersContaining(userB)) {
            throw new IllegalStateException("User B is already in a game");
        }

        Game newGame = new Game();
        newGame.getUsers().add(userA);
        newGame.getUsers().add(userB);
        System.out.println("vor speichern");
        gameRepository.save(newGame);
        System.out.println("Game gespeichert");

        return new GameWithUsersDTO(newGame.getId(), Arrays.asList(userA, userB));
    }








    public Game selectDeck(DeckSelectionRequest request) {
        return null;
    }

    public Game playCard(PlayCardRequest request) {
        return null;
    }

    public Game endTurn(EndTurnRequest request) {
        return null;
    }

    public Game attackCard(AttackCardRequest request) {
        return null;
    }

    public Game attackUser(AttackUserRequest request) {
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

    public static void terminateMatch(Long gameID) {
        // Implementierung
    }


}
