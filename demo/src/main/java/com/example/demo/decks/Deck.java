package com.example.demo.decks;

import com.example.demo.cards.Card;
import com.example.demo.user.UserAccount;
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
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "deck_card",
            joinColumns = @JoinColumn(name = "deck_id"),
            inverseJoinColumns = @JoinColumn(name = "card_id")
    )
    private List<Card> cards;
    @ManyToOne
    @JoinColumn
    private UserAccount user;


}
