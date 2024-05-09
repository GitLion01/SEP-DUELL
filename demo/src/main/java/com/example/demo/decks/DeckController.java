package com.example.demo.decks;

import com.example.demo.cards.Card;
import com.example.demo.cards.CardRepository;
import com.example.demo.cards.CardService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(path = "/decks")
@CrossOrigin
@AllArgsConstructor
public class DeckController {

    private final DeckRepository deckRepository;
    @Autowired
    private final DeckService deckService;
    private final CardRepository cardRepository;


    // TODO: Check via frontend (throws NullPointerException because no User)
    @PostMapping(path = "/create")
    public String createDeck(@RequestBody DeckRequest request/*, Principal principal*/) {
        return deckService.createDeck(request/*, principal*/);
    }

    @PutMapping("/updateName/{oldName}/{newName}") //Decknamen dürfen keine Leerzeichen enthalten
    public String updateDeckName(@PathVariable String oldName, @PathVariable String newName) {
        return deckService.updateDeckName(oldName, newName);
    }

    @PutMapping("/removeCards")
    public String removeCardsFromDeck(@RequestBody Map<String, Object> requestBody) {
        String deckName = (String) requestBody.get("deckName");
        List<String> cardsToRemove = (List<String>) requestBody.get("cardsToRemove");
        return deckService.removeCards(deckName, cardsToRemove);
    }


    @PutMapping("/addCards")
    public String addCardsToDeck(@RequestBody Map<String, Object> requestBody) {
        String deckName = (String) requestBody.get("deckName");
        List<String> cardsToAdd = (List<String>) requestBody.get("cardsToAdd");
        return deckService.addCardsToDeck(deckName, cardsToAdd);
    }

    // TODO: Check
    @PutMapping("/replaceCards/{deckName}")
    public String replaceCardsInDeck(@PathVariable String deckName, @RequestBody ReplaceCardsRequest request) {
        return deckService.replaceCardsInDeck(deckName, request.getCardsToRemove(), request.getCardsToAdd());
    }


    @GetMapping("/{deckName}/cards")//Decknamen dürfen keine Leerzeichen enthalten
    public List<Card> getAllCardsFromDeck(@PathVariable String deckName) {
        return deckService.getAllCardsFromDeck(deckName);
    }

    // TODO: Check via frontend
    @GetMapping(path = "/getUserDecks")
    public List<Deck> getUserDecks(@RequestBody String email) {
        return deckService.getUserDecksByEmail(email);
    }
}


