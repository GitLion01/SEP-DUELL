package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class RareSwapRequest {

    Long gameId;
    Long userId;
    List<Integer> normalCardsIndex;
    int rareCardIndex;
}
