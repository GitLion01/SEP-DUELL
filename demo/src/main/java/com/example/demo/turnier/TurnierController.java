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

    @MessageMapping("/turnierAblehnen")
    public void turnierAblehnen(@Payload Long userId) {
        turnierService.turnierAblehnen(userId);
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

    @MessageMapping("/gewinnerSpeichern")
    public void gewinnerSpeichern(@Payload Long userId) {
        turnierService.GewinnerSpeichernMitId(userId);
    }

    @GetMapping("/checkAccepted")
    public boolean checkAccepted(@RequestParam Long turnierId,@RequestParam Long userId) {
        return turnierService.checkAccepted(turnierId,userId);
    }

    @GetMapping("/getTurnierId")
    public Long getTurnierId(@RequestParam Long clanId) {
        return turnierService.getTurnierId(clanId);
    }

    @GetMapping("/getGewinner")
    public List<Long> getGewinner(@RequestParam Long clanId) {
        return turnierService.getGewinner(clanId);
    }
}
