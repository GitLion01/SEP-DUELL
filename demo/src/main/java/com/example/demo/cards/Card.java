package com.example.demo.cards;


import com.example.demo.decks.Deck;
import com.example.demo.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    private Integer attackPoints;
    private Integer defensePoints;
    private String description;
    private byte[] image;
    private Rarity rarity;
    @ManyToMany
    private List<Deck> decks = new ArrayList<>();


    @JsonIgnore
    @ManyToMany(mappedBy = "cards")
    private List<UserAccount> users = new ArrayList<>();

    public Card(String name,
                Integer attackPoints,
                Integer defensePoints,
                String description,
                byte[] image,
                Rarity rarity) {

        this.name = name;
        this.attackPoints = attackPoints;
        this.defensePoints = defensePoints;
        this.description = description;
        this.image = image;
        this.rarity = rarity;
    }
}
