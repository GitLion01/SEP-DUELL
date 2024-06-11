package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayCardRequest {

    private Long gameId;
    private Long userId;
    private Long cardIndex;
}
