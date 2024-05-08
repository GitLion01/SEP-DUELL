package com.example.demo.cards;


import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
    public String deleteCard(@PathVariable String name) {
        try{
            return cardService.deleteCard(name);
        }catch (Exception e){
            System.out.println("card does not exist"); // für Ausgabe in Konsole
            return "card does not exist"; // für Ausgabe in Postman
        }

    }




    @GetMapping(path = "/findByName/{name}")
    public Optional<Card> findByByName(@PathVariable String name) {
        try {
            return cardRepository.findByName(name);
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    @GetMapping
    public List<Card> findAll(){return cardRepository.findAll();}
}
