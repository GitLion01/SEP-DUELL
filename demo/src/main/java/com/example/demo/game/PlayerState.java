package com.example.demo.game;

import com.example.demo.cards.Card;
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
    private Boolean ready = false;



    @OneToOne(mappedBy = "playerState" , cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Transient
    private List<Card> cardsPlayed = new ArrayList<>();

    @Transient
    private List<Card> handCards = new ArrayList<>();

    @Transient
    private List<Card> fieldCards = new ArrayList<>();

    // Deck ist nicht mit Deck Tabelle verbunden, Änderungen beeinflussen die Deck Tabelle nicht und sind
    // nur für den Lebenszyklus des Programms gespeichert (wird der server neu gestartet sind die Änderungen verworfen
    @Transient
    private Deck deck;




}
