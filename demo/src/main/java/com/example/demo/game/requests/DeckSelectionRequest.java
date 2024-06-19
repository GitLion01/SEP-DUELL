package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DeckSelectionRequest {

    private Long gameId;
    private Long deckId;
    private Long userId;

}
