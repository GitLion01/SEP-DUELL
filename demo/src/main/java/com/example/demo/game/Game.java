package com.example.demo.game;

import com.example.demo.cards.Card;
import com.example.demo.cards.CardInstance;
import com.example.demo.decks.Deck;
import com.example.demo.user.UserAccount;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;


    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<UserAccount> players;

    // 0 oder 1
    private Integer currentTurn;


}
