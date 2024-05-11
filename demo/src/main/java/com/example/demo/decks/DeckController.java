package com.example.demo.decks;

import com.example.demo.cards.Card;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;



@RestController
@RequestMapping(path = "/decks")
@CrossOrigin
@AllArgsConstructor
public class DeckController {


    @Autowired
    private final DeckService deckService;
    @Autowired
    private DeckRepository deckRepository;


    @PostMapping(path = "/create")
    public String createDeck(@RequestBody DeckRequest request) {
        return deckService.createDeck(request);
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


    @PutMapping("/replaceCards/{deckName}/{userID}")//Decknamen dürfen keine Leerzeichen enthalten
    public String replaceCardsInDeck(@PathVariable String deckName, @PathVariable Long userID, @RequestBody ReplaceCardsRequest request) {
        return deckService.replaceCardsInDeck(deckName, userID, request.getCardsToRemove(), request.getCardsToAdd());
    }


    @GetMapping("/cards/{deckName}/{userID}")//Decknamen dürfen keine Leerzeichen enthalten
    public List<Card> getAllCardsFromDeck(@PathVariable Long userID, @PathVariable String deckName) {
        return deckService.getAllCardsFromDeck(userID, deckName);
    }



    @GetMapping(path = "/getUserDecks/{userID}")
    public List<Deck> getUserDecks(@PathVariable Long userID) {
        return deckService.getUserDecksByUserId(userID);
    }

    @GetMapping(path = "/getAll")
    public List<Deck> getAll() {
        return deckRepository.findAll();
    }


    @DeleteMapping("/delete")
    public String deleteDeck(@RequestBody Map<Long, String> requestBody) {
        Long userId = requestBody.keySet().iterator().next();
        String deckName = requestBody.get(userId);
        return deckService.deleteDeckByUserIdAndDeckName(userId, deckName);
    }

}