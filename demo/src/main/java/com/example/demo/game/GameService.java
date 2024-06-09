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
import org.springframework.transaction.annotation.Transactional;

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
