package com.example.demo.game;

import com.example.demo.cards.CardInstance;
import com.example.demo.cards.Rarity;
import com.example.demo.decks.Deck;
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
public class PlayerCard {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    private Integer attackPoints;
    private Integer defensePoints;
    private String description;
    private byte[] image;
    private Rarity rarity;
    //CascadeType.All funktioniert auch , das umfasst :
    //CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH und CascadeType.DETACH.

    /*@ManyToMany(mappedBy = "cards", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Deck> decks = new ArrayList<>();*/

    /*@OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardInstance> cardInstance=new ArrayList<>();*/

    @ManyToOne
    @JsonIgnore
    private PlayerState playerState;
    private Boolean hasAttacked = false;
    private Boolean sacrificed = false;

    public PlayerCard(String name,
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
