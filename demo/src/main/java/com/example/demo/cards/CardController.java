package com.example.demo.cards;



import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ResponseEntity<String> deleteCard(@RequestBody List<String> names) {
        if (!names.isEmpty())
            return ResponseEntity.ok(cardService.deleteMultipleCards(names));
        else
            return ResponseEntity.badRequest().body("At least one card name is required.");
    }




    @GetMapping(path = "/findByName/{name}")
    public ResponseEntity<Optional<CardResponse>> findByByName(@PathVariable String name) {
        try {
            Optional<Card> card = cardRepository.findByName(name);
            // prüft ob die Karte existiert
            if (card.isPresent()) {
                // Konvertiere das Bild von byte[] zu Base64
                String base64Image = Base64.getEncoder().encodeToString(card.get().getImage());
                // Erstelle eine CardResponse mit dem Base64-Bild und anderen Details
                CardResponse cardResponse = new CardResponse(card.get().getName(), card.get().getAttackPoints(), card.get().getDefensePoints(), card.get().getDescription(), base64Image, card.get().getRarity());
                return ResponseEntity.ok(Optional.of(cardResponse)); // erstellt eine HTTP-Antwort mit status 200 ok
            } else {
                return ResponseEntity.notFound().build(); // erstellt eine HTTP-Antwort mit status 404 Not Found
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping
    public ResponseEntity<List<CardResponse>> findAll() {
        List<Card> cards = cardRepository.findAll();
        List<CardResponse> cardResponses = cards.stream().map(card -> {
            // Konvertiere das Bild jeder Karte von byte[] zu Base64
            String base64Image = Base64.getEncoder().encodeToString(card.getImage());
            // Erstelle eine CardResponse mit dem Base64-Bild und anderen Details
            return new CardResponse(card.getName(), card.getAttackPoints(), card.getDefensePoints(), card.getDescription(), base64Image, card.getRarity());
        }).collect(Collectors.toList()); // sammelt die Kartenobjekte in einer Liste
        return ResponseEntity.ok(cardResponses);
    }
}
