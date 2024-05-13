package com.example.demo.cards;

import jakarta.persistence.Lob;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class CardResponse {
    private String name;
    private Integer attackPoints;
    private Integer defensePoints;
    private String description;
    private String image; // Hier wird das Bild als Base64-String gespeichert
    private Rarity rarity;

}