package com.example.demo.cards;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(path = "/cards")
@CrossOrigin
@AllArgsConstructor
public class CardController {

    private final CardService cardService;
    private final CardRepository cardRepository;




    @PostMapping("/upload")
    public ResponseEntity<String> uploadCards(@RequestParam("file") MultipartFile file) {
        String response = cardService.uploadAndSaveCards(file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/delete/{name}")
    public ResponseEntity<String> deleteCard(@PathVariable String name) {
        String message = cardService.deleteCard(name);
        return ResponseEntity.ok(message);
    }

    @GetMapping
    public List<Card> findAll(){return cardRepository.findAll();}
}
