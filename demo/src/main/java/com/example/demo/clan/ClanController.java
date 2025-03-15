package com.example.demo.clan;


import com.example.demo.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@AllArgsConstructor
public class ClanController {

    private final ClanService clanService;

    @PostMapping("/createClan")
    public ResponseEntity<Object> createClan(@RequestParam String ClanName){
        return clanService.createClan(ClanName);
    }

    @GetMapping("/getClans")
    public ResponseEntity<List<ClanDTO>> getAllClans(){
        return clanService.getClans();
    }

    @GetMapping("/getClanId")
    public ResponseEntity<Long> getClanId(@RequestParam Long userId){
        return clanService.getClanId(userId);
    }

    @GetMapping("/getClanMitglieder")
    public ResponseEntity<List<UserDTO>> getClanMitglieder(@RequestParam Long clanId){
        return clanService.getClanMitglieder(clanId);
    }

    @PostMapping("/joinClan")
    public ResponseEntity<String> joinClan(@RequestParam Long clanId,@RequestParam Long UserId){
        return clanService.joinClan(clanId,UserId);
    }

    @PostMapping("/leaveClan")
    public ResponseEntity<String> leaveClan(@RequestParam Long userId){
        return clanService.leaveClan(userId);
    }
}
