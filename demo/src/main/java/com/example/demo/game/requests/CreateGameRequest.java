package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateGameRequest {

    private Long gameId;
    private Long userId;


}
