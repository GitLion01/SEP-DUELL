package com.example.demo.cards;


import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.user.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


@Service
public class CardService {


    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    @Autowired
    public CardService(CardRepository cardRepository, DeckRepository deckRepository) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
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
            public void deleteCard(String name){
                Optional<Card> optionalCard = cardRepository.findByName(name);
                if (optionalCard.isEmpty()) {
                    return;
                }
                Card card = optionalCard.get();

                // Create a copy of the list of users associated with the card
                List<UserAccount> usersCopy = new ArrayList<>(card.getUsers());

                // Iterate over the copy and remove the card from each user's collection
                for (UserAccount user : usersCopy) {
                    user.removeCard(card);
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
                    byte[] image = parts[5].trim().getBytes(); // Load image data properly
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





    public String uploadAndSaveCards(MultipartFile file) {
        try {
            List<CardRequest> cardRequests = parseCSV(file);
            return addCards(cardRequests);
        } catch (IOException e) {
            return "Error uploading CSV: " + e.getMessage();
        }
    }
    }



