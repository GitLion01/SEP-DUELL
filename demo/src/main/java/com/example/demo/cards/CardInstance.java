package com.example.demo.cards;
import com.example.demo.game.PlayerState;
import com.example.demo.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class CardInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // sobald ein Deck für das Spiel ausgewählt wird, ändert es sich zu FIELD oder HAND
    // ist eine Karte zerstört, dann wieder zu NONE
    private CardInstanceLocation location = CardInstanceLocation.NONE;

    @ManyToOne
    @JoinColumn(name= "card_id")
    @JsonIgnore
    private Card card;

    @ManyToOne
    @JoinColumn(name= "user_id")
    @JsonIgnore
    private UserAccount userAccount;

    @ManyToOne
    @JoinColumn
    private PlayerState playerState;
}
