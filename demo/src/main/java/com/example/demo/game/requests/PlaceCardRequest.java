package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlaceCardRequest {

    private Long gameId;
    private Long userId;
    private Long cardId;
}
