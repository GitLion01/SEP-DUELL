package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AttackCardRequest {

    private Long gameId;
    private Long userIdAttacker;
    private Long userIdDefender;
    // angreifende Karte von Angreifer
    private Long attackerId;
    // verteidigende Karte von Verteidiger
    private Long targetId;

}
