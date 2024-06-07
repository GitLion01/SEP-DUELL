package com.example.demo.game;

import com.example.demo.cards.CardInstance;
import com.example.demo.decks.Deck;
import com.example.demo.user.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class PlayerState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id; //stateID
    private Integer lifePoints = 50;
    private Integer damage;
    private Integer cardsPlayed;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private UserAccount user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardInstance> hand = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardInstance> field = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "deck_id")
    private Deck deck;

}
