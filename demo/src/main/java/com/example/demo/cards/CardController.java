package com.example.demo.cards;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/cards")
@CrossOrigin
@AllArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping(path = "/add")
    public String addCard(@RequestBody CardRequest request) {
        return cardService.addCard(request);
    }

    @GetMapping()
    public String deleteCard(@RequestBody CardRequest request) {return cardService.deleteCard(request);}
}
