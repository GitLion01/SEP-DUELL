package com.example.demo.game.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttackBotRequest {
    private Long gameId;
    private Long attackerId;
    private Long botPSId;
    private Long attackerCardId;
}
