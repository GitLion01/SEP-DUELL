package com.example.demo.cards;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class CardRequest {
    private String name;
    private Integer attackPoints;
    private Integer defensePoints;
    private String description;
    private String image;
    private Rarity rarity;
}

