package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EndTurnRequest {

    private Long gameID;
    private Long userID;

}
