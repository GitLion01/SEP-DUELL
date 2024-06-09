package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AttackCardRequest {

    private Long gameID;
    private Long userIDAttacker;
    private Long userIDDefender;
    // angreifende Karte von Angreifer
    private Long attackerIndex;
    // verteidigende Karte von Verteidiger
    private Long targetIndex;

}
