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
    private Integer damage;
    private Boolean ready = false;
    private Boolean winner = false;



    @OneToOne(mappedBy = "playerState" , cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @OneToMany
    private List<CardInstance> cardsPlayed = new ArrayList<>();

    @OneToMany
    private List<CardInstance> handCards = new ArrayList<>();

    @OneToMany
    private List<CardInstance> fieldCards = new ArrayList<>();


    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "deck_id")
    private Deck deck;




}
