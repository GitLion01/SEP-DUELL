package com.example.demo.cards;


import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Transactional
@Service
public class CardService {


    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserAccountRepository userAccountRepository;
    private final CardInstanceRepository cardInstanceRepository;

    @Autowired
    public CardService(CardRepository cardRepository, DeckRepository deckRepository, UserAccountRepository userAccountRepository, CardInstanceRepository cardInstanceRepository) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.userAccountRepository = userAccountRepository;
        this.cardInstanceRepository = cardInstanceRepository;
    }


    public String addCards(List<CardRequest> requests) {
        List<String> addedCards = new ArrayList<>();
        List<UserAccount> allUsers = userAccountRepository.findAll();
        for (CardRequest request : requests) {
            try {
                saveCard(request);
                addedCards.add(request.getName());

                // add card for all users
                for (UserAccount userAccount : allUsers) {
                    List<CardInstance> allCardInstances = cardInstanceRepository.findByUserAccount(userAccount);

                    boolean cardExists = false;

                    // Check if the card already exists for the user
                    for (CardInstance cardInstance : allCardInstances) {
                        if (cardInstance.getCard().getName().equals(request.getName())) {
                            cardExists = true;
                            break;
                        }
                    }

                    // If the card doesn't exist for the user, add it
                    if (!cardExists) {
                        addCardsInstanzen(userAccount.getId(), Collections.singletonList(request.getName()));
                    }
                }
            } catch (IllegalStateException e) {
                System.out.println("Error adding card: " + e.getMessage());
            }
        }
        return "Added cards: " + String.join(", ", addedCards);
    }

    private void saveCard(CardRequest request) throws IllegalStateException {
        boolean cardExists = cardRepository.existsByName(request.getName());
        if (cardExists) {
            throw new IllegalStateException("Card already exists: " + request.getName());
        }
            Card card = new Card(
                    request.getName(),
                    request.getAttackPoints(),
                    request.getDefensePoints(),
                    request.getDescription(),
                    request.getImage(),
                    request.getRarity()
            );
            cardRepository.save(card);
}


    public void deleteCard(String name) {
        // prüft ob die Karte bereits existiert
        Optional<Card> optionalCard = cardRepository.findByName(name);
        if (optionalCard.isEmpty()) {
            return;
        }
        // Karte hier nicht mehr vom Typ Optional
        Card card = optionalCard.get();

        // findet alle Decks in der die Karte enthalten ist
        List<Deck> decksContainingCard = deckRepository.findByCardsContaining(card);

        // extrahiert die IDs der Karte
        List<Long> cardIds = new ArrayList<>();
        cardIds.add(card.getId());


        // iteriert über jedes Deck, was die Karte enthält und löscht die Karteninstanzen
        for (Deck deck : decksContainingCard) {
            deckRepository.deleteDeckCardsByDeckIdAndCardIds(deck.getId(), cardIds);
        }

        // löscht die Karte aus dem Spiel (es mussten zuerst alle Karten aus den anderen tabellen gelöscht werden wegen Fremdschlüsselverweis)
        cardRepository.delete(card);
    }


    public String deleteMultipleCards(List<String> names){
        for (String name : names) {
            deleteCard(name);
        }
        return "Cards deleted";
    }



    public List<CardRequest> parseCSV(MultipartFile file) throws IOException, NumberFormatException {
        List<CardRequest> cardRequests = new ArrayList<>();
        try (InputStream is = file.getInputStream(); // erschafft ein Input Stream von einer Datei (hier zuerst byte-Stream
             // bietet Methoden für leseoperationen
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) { //StandardCharsets.UTF_8 wandelt bytes in unicode-Zeichen um

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // Zeile wird in Teile durch Komma zerlegt
                if (parts.length == 6) { // es dürfen max 6 Teile entstehen
                    String name = parts[0].trim();
                    int attackPoints = parts[2].equalsIgnoreCase("unlimited") ? Integer.MAX_VALUE : Integer.parseInt(parts[2].trim()); // keine Kommazahl möglich
                    int defensePoints = parts[3].equalsIgnoreCase("unlimited") ? Integer.MAX_VALUE : Integer.parseInt(parts[3].trim());
                    String description = parts[4].trim();
                    if (description.length() > 200) {
                        description = description.substring(0, 200);
                    }
                   /* byte[] image = parts[5].trim().getBytes(); // Load image data properly*/
                    byte[] image = loadImageFromFile(parts[5].trim());
                    // Load image from file system and encode to Base64
                    /*String image = loadImageAndEncodeToBase64(parts[5].trim());*/
                    Rarity rarity = Rarity.valueOf(parts[1].trim().toUpperCase());

                    // erstellt Reqeusts aus den Daten und fügt sie einer Liste hinzu. Sie werdne für die Erstellung der Karteninstanzen benötigt
                    CardRequest cardRequest = new CardRequest(name, attackPoints, defensePoints, description, image, rarity);
                    cardRequests.add(cardRequest);
                } else {
                    System.out.println("Ungültige Zeile: " + line);
                }
            }
        }
        return cardRequests;
    }

    // extrahiert das Bild aus der angegebenen Datei
    private byte[] loadImageFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }
    private String loadImageAndEncodeToBase64(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] imageData = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(imageData);
    }

    public String uploadAndSaveCards(MultipartFile file) {
        try {
            List<CardRequest> cardRequests = parseCSV(file);
            return addCards(cardRequests);
        } catch (IOException e) {
            return "Error uploading CSV: " + e.getMessage();
        }
    }

    public ResponseEntity<String> addCardsInstanzen(Long userID, List<String> cards) {
        Optional<UserAccount> user = userAccountRepository.findById(userID);
        if(user.isPresent()) {
            UserAccount userAccount = user.get();
            for (String s : cards) {
                Optional<Card> cardOptional = cardRepository.findByName(s);
                if (cardOptional.isPresent()) {
                    Card card = cardOptional.get();

                    CardInstance cardInstance = new CardInstance();

                    cardInstance.setCard(card);
                    card.getCardInstance().add(cardInstance);

                    cardInstance.setUserAccount(userAccount); // Setzen Sie den UserAccount in der CardInstance
                    userAccount.getUserCardInstance().add(cardInstance);

                    cardInstanceRepository.save(cardInstance);
                    cardRepository.save(card);
                    userAccountRepository.save(userAccount);
                }
            }
            return ResponseEntity.ok("Cards added");
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}



