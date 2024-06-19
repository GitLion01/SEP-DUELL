package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AttackUserRequest {

    private Long gameId;
    private Long attackerId;
    private Long defenderId;
    private Long attackerCardId;
}
