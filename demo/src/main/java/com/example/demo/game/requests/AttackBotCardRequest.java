package com.example.demo.game.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttackBotCardRequest {
    Long gameId;
    Long userIdAttacker;
    Long attackerId;
    Long targetId;
    Long botPSId;
}
