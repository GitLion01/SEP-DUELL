package com.example.demo.decks;

import com.example.demo.cards.Card;
import com.example.demo.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "deck_card",
            // deck_id und card_id sind zusammengesetzter Primärschlüssel in Ergebnistabelle
            joinColumns = @JoinColumn(name = "deck_id"),
            inverseJoinColumns = @JoinColumn(name = "card_id")
    )
    private List<Card> cards;
    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private UserAccount user;




}
