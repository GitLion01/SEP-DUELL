package com.example.demo.cards;
import com.example.demo.decks.Deck;
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
    //CascadeType.All funktioniert auch , das umfasst :
    //CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH und CascadeType.DETACH.

    @ManyToMany(mappedBy = "cards", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Deck> decks = new ArrayList<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardInstance> cardInstance=new ArrayList<>();




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
