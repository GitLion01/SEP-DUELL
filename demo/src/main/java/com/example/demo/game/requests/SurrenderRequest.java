package com.example.demo.game.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SurrenderRequest {
    Long gameId;
    Long userId;
}
