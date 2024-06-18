package com.example.demo.game;

import com.example.demo.cards.Card;
import com.example.demo.cards.CardInstance;
import com.example.demo.decks.Deck;
import com.example.demo.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Integer damage = 0;
    private Boolean ready = false;
    private Boolean winner = false;



    @OneToOne(mappedBy = "playerState")
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @OneToMany(fetch = FetchType.EAGER)
    private List<PlayerCard> cardsPlayed = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    private List<PlayerCard> handCards = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    private List<PlayerCard> fieldCards = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    private List<PlayerCard> deckClone = new ArrayList<>();


    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "deck_id")
    private Deck deck;




}
