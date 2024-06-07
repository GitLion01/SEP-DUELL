package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AttackRequest {

    private Long gameID;
    private Long userIDAttacker;
    private Long userIDDefender;
    // angreifende Karte von Angreifer
    private Long attackerID;
    // verteidigende Karte von Verteidiger
    private Long targetID;

}
