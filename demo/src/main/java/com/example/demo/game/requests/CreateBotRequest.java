package com.example.demo.game.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateBotRequest {
    Long userId;
    Long deckId;
    Boolean streamed;
}
