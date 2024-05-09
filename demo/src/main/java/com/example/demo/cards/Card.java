package com.example.demo.cards;


import com.example.demo.decks.Deck;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @ManyToOne
    private Deck decks;



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
