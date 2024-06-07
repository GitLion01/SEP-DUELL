package com.example.demo.game.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class RareSwapRequest {

    Long gameID;
    Long userID;
    Long selectedDeckID;
    List<Long> normalCardsID;
    Long RareCardID;
}
