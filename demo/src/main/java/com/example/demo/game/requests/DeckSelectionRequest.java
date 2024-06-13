package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeckSelectionRequest {

    private Long gameId;
    private Long deckId;
    private Long userId;

}
