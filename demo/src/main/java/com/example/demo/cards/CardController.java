package com.example.demo.cards;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/cards")
@CrossOrigin
@AllArgsConstructor
public class CardController {

    private final CardService cardService;
    private final CardRepository cardRepository;

    @PostMapping(path = "/add")
    public String addCard(@RequestBody CardRequest request) {
        return cardService.addCard(request);
    }

    @PostMapping(path = "/delete")
    public ResponseEntity<String> deleteCard(@RequestBody List<String> names) {
        if (!names.isEmpty()) {
            String message = cardService.deleteMultipleCards(names);
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.badRequest().body("At least one card name is required.");
        }
    }

    @GetMapping
    public List<Card> findAll(){return cardRepository.findAll();}
}
