package com.example.demo.decks;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ReplaceCardsRequest {
    private List<String> cardsToRemove;
    private List<String> cardsToAdd;

}
