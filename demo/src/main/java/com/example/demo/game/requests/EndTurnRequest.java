package com.example.demo.game.requests;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class EndTurnRequest {

    private Long gameID;
    private Long userID;

}
