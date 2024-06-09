package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AttackUserRequest {

    Long attackerID;
    Long defenderID;
    Long gameID;
    Long attackerCardID;
}
