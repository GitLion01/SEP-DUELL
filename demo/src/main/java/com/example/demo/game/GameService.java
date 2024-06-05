package com.example.demo.game;

import com.example.demo.user.UserAccount;
import jakarta.persistence.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerState> playerStates = new ArrayList<>();
    private UserAccount currentTurn;






    public Game joinGame(JoinRequest request) {
        // Implementierung
        return null;
    }


    public Game selectDeck(DeckSelectionRequest request) {
        // Implementierung
        return null;
    }


    public Game playCard(PlayCardRequest request) {
        // Implementierung
        return null;
    }


    public Game attack(AttackRequest request) {
        // Implementierung
        return null;
    }


    public Game endTurn(EndTurnRequest request) {
        // Implementierung
        return null;
    }
}
