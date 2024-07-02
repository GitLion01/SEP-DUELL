package com.example.demo.turnier;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@AllArgsConstructor
public class TurnierController {

    private final TurnierService turnierService;

    @MessageMapping("/turnierStart")
    public void turnierStart(@Payload Long clanId) {
        turnierService.turnierStart(clanId);
    }

    @MessageMapping("/turnierAkzeptieren")
    public void turnierAkzeptieren(@Payload Long userId) {
        turnierService.turnierAkzeptieren(userId);
    }

    @GetMapping("/getTurnier")
    public ResponseEntity<List<Match>> getTurnierMatches(@RequestParam Long clanId) {
        return turnierService.getTurnierMatches(clanId);
    }

    @GetMapping("/checkTurnier")
    public boolean checkTurnier(@RequestParam Long turnierId) {
        return turnierService.turnierIsReady(turnierId);
    }

    @PutMapping("/setUserInTurnier")
    public void setUserInTurnier(@RequestParam Long userId) {
        turnierService.SetUserInTurnier(userId);
    }

    @PostMapping("/gewinnerSpeichern")
    public void gewinnerSpeichern(@RequestParam Long userId) {
        turnierService.GewinnerSpeichernMitId(userId);
    }
}
