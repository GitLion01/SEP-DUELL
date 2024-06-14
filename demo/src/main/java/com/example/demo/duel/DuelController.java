package com.example.demo.duel;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DuelController {

    private final DuelService duelService;

    @MessageMapping("/challenge")
    public void challengeUser(@Payload DuelRequest request) {
        duelService.challengeUser(request.getChallengerId(), request.getChallengedId());
    }

    @MessageMapping("/respond")
    public void respondToChallenge(@Payload DuelResponse request) {
        duelService.respondToChallenge(request.getChallengedId(), request.isAccepted());
    }
}
