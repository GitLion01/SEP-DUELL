package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class LegendarySwapRequest {

    Long gameId;
    Long userId;
    List<Long> cardIds;
    Long legendaryId;

}
