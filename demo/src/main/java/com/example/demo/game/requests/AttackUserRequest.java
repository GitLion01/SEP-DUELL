package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AttackUserRequest {

    private Long gameId;
    private Long attackerId;
    private Long defenderId;
    private int attackerCardIndex;
}
