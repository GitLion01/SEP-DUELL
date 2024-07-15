package com.example.demo.turnier;


import com.example.demo.clan.ClanIdRequest;
import com.example.demo.clan.UserIdRequest;
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


    @PostMapping("/turnierStart")
    public void turnierStart(@RequestBody ClanIdRequest request) {
        turnierService.turnierStart(request.getClanId());
    }

    @PostMapping("/turnierAkzeptieren")
    public void turnierAkzeptieren(@RequestBody UserIdRequest request) {
        turnierService.turnierAkzeptieren(request.getUserId());
    }

    @PostMapping("/turnierAblehnen")
    public void turnierAblehnen(@RequestBody UserIdRequest request) {
        turnierService.turnierAblehnen(request.getUserId());
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

    @GetMapping("/checkAccepted")
    public boolean checkAccepted(@RequestParam Long turnierId,@RequestParam Long userId) {
        return turnierService.checkAccepted(turnierId,userId);
    }

    @GetMapping("/getTurnierId")
    public ResponseEntity<Long> getTurnierId(@RequestParam Long clanId) {
        return turnierService.getTurnierId(clanId);
    }

    @MessageMapping("/UserStatus")
    public void userStatus(@Payload Long turnierID) {
        turnierService.getUserStatus(turnierID);
    }



// Turnierwette

    @PostMapping("/placeBet")
    public ResponseEntity<String> placeBet(@RequestBody BetRequest request) {
        return turnierService.placeBet(request.getBettorId(), request.getBetOnId());
    }

    @GetMapping("/getGewinner")
    public List<Long> getGewinner(@RequestParam Long clanId) {
        return turnierService.getGewinner(clanId);
    }
}
