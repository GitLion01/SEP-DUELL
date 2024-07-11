package com.example.demo.game;

import com.example.demo.user.UserAccount;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameDTO {

    private Long id;
    private Boolean ready;
    private Boolean firstRound;
    private Integer timeLeft;
    private Boolean streamed;
    private Long botDeckId;
    private PlayerState playerStateBot;
    private UserAccount currentTurn;
    private Boolean myTurn;
}
