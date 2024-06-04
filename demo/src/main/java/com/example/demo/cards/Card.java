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
    @ManyToOne
    private UserAccount userAccounts;




    public Card(String name,
                Integer attackPoints,
                Integer defensePoints,
                String description,
                byte[] image,
                Rarity rarity) {

        this.name = name;
        this.attackPoints = attackPoints;
        this.defensePoints = defensePoints;

        // Überprüfe, ob die Beschreibung maximal 200 Zeichen lang ist
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("Description must be at most 200 characters long.");
        }
        this.description = description;

        this.image = image;
        this.rarity = rarity;
    }
}
