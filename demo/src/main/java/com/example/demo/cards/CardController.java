package com.example.demo.cards;



import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ResponseEntity<?> uploadCardsFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            String result = cardService.uploadAndSaveCards(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Hochladen und Speichern der Karten aus der CSV-Datei.");
        }
    }


    @PostMapping(path = "/delete")
    public String deleteCard(@RequestBody List<String> names) {
        if (!names.isEmpty())
            return cardService.deleteMultipleCards(names);
         else
            return "At least one card name is required.";
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
