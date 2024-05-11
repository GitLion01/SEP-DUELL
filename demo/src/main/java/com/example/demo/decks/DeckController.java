package com.example.demo.decks;

import com.example.demo.cards.Card;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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




   /* @PutMapping("/updateName/{oldName}/{newName}") //Decknamen dürfen keine Leerzeichen enthalten
    public String updateDeckName(@PathVariable String oldName, @PathVariable String newName) {
        return deckService.updateDeckName(oldName, newName);
    }*/
    @PutMapping("/updateName/{userId}/{oldName}/{newName}")
    public ResponseEntity<String> updateDeckName(
            @PathVariable Long userId,
            @PathVariable String oldName,
            @PathVariable String newName) {

        String result = deckService.updateDeckName(userId, oldName, newName);
        if (result.contains("Fehler")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }


    @PutMapping("/removeCards")
    public String removeCardsFromDeck(@RequestBody DeckRequest request) {
        return deckService.removeCards(request);
    }


    /*@PutMapping("/addCards")
    public String addCardsToDeck(DeckRequest request) {
        return deckService.addCardsToDeck(request);
    }*/
    // TODO: userID muss berücksichtigt werden -> deck not found
    @PutMapping("/addCards")
    public String addCardsToDeck(@RequestBody DeckRequest request) {
        // Loggen der empfangenen Parameter
        System.out.println("UserID: " + request.getUserID());
        System.out.println("DeckName: " + request.getName());
        System.out.println("CardNames: " + request.getCardNames());

        return deckService.addCardsToDeck(request);
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


