package com.example.demo.game;
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



    @OneToOne(fetch = FetchType.EAGER,mappedBy = "playerState")
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserAccount user;

    @OneToMany
    private List<PlayerCard> cardsPlayed = new ArrayList<>();

    @OneToMany
    private List<PlayerCard> handCards = new ArrayList<>();

    @OneToMany
    private List<PlayerCard> fieldCards = new ArrayList<>();

    @OneToMany
    private List<PlayerCard> deckClone = new ArrayList<>();


    @OneToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "deck_id")
    private Deck deck;




}
