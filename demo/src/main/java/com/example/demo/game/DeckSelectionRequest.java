package com.example.demo.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeckSelectionRequest {

    private Long gameID;
    private Long deckID;
    private Long userID;

}
