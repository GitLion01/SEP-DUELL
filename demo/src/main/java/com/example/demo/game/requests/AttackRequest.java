package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AttackRequest {

    private Long gameID;
    private Long userID;
    // angreifende Karte
    private Long attackerID;
    // verteidigende Karte
    private Long targetID;

}
