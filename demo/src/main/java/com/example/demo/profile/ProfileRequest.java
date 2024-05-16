package com.example.demo.profile;
import com.example.demo.cards.Card;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ProfileRequest {
    private String name;
    private int SEPCoins;
    private int leaderboardPoints;
    private int cards;
    private List<Card> cardList;
}
