package com.example.demo.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EndTurnRequest {

    private Long gameID;
    private Long userID;

}
