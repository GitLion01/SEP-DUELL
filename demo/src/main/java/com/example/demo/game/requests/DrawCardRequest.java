package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DrawCardRequest {

    private Long gameId;
    private Long userId;
}
