package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayCardRequest {

    private Long gameID;
    private Long userID;
    private Long cardInstanceID;
}
