package com.example.demo.cards;
import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
        for (CardRequest request : requests) {
            try {
                saveCard(request);
                addedCards.add(request.getName());
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
        Optional<Card> optionalCard = cardRepository.findByName(name);
        if (optionalCard.isEmpty()) {
            return;
        }
        Card card = optionalCard.get();

        // Find all decks containing the card
        List<Deck> decksContainingCard = deckRepository.findByCardsContaining(card);

        // Extract card IDs
        List<Long> cardIds = new ArrayList<>();
        cardIds.add(card.getId());

        // Iterate over each deck containing the card and remove the card from it using deleteDeckCardsByDeckIdAndCardIds method
        for (Deck deck : decksContainingCard) {
            deckRepository.deleteDeckCardsByDeckIdAndCardIds(deck.getId(), cardIds);
        }

        cardRepository.delete(card);
    }

    public String deleteMultipleCards(List<String> names){
        for (String name : names) {
            deleteCard(name);
        }
        return "Cards deleted";
    }

    public List<CardRequest> parseCSV(MultipartFile file) throws IOException {
        List<CardRequest> cardRequests = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String name = parts[0].trim();
                    int attackPoints = parts[2].equalsIgnoreCase("unlimited") ? Integer.MAX_VALUE : Integer.parseInt(parts[2].trim());
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

                    CardRequest cardRequest = new CardRequest(name, attackPoints, defensePoints, description, image, rarity);
                    cardRequests.add(cardRequest);
                } else {
                    System.out.println("Ungültige Zeile: " + line);
                }
            }
        }
        return cardRequests;
    }

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
                    cardInstance.setCount(cardInstance.getCount() + 1);

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



