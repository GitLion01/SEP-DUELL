package com.example.demo.decks;

import com.example.demo.cards.Card;
import com.example.demo.cards.CardRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import com.example.demo.user.UserAccountService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class DeckService{

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private UserAccountService userAccountService;
    // TODO: Check for alternative implementation
    private UserAccount userAccount;

    // TODO: Check if necessary
    private UserAccount getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserAccount) {
                return (UserAccount) authentication.getPrincipal();
            } else {
                // Handle the case where the current user is not authenticated or is not a UserAccount
                return null;
            }
        } catch (Exception e) {
            // Handle any potential exceptions, such as ClassCastException
            e.getMessage();
            return null;
        }
    }




    @Autowired
    public DeckService(DeckRepository deckRepository, CardRepository cardRepository) {
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
    }


    public boolean isDeckNameAvailable(String deckName) {
        Optional<Deck> existingDeck = deckRepository.findByName(deckName);
        return existingDeck.isEmpty(); // Returns true if no deck with the given name exists
    }


    public String createDeck(@RequestBody DeckRequest request) {


        int userDeckCount = deckRepository.countByUserId(request.getUserID());
        if (userDeckCount >= 3) {
            return "Error: Maximum of 3 decks per user allowed";
        }

        // check if deck with this name alredy exists
        String deckName = request.getName();
        if (!isDeckNameAvailable(deckName)) {
            return "Error: A deck with the name '" + deckName + "' already exists";
        }

        List<String> cardNames = request.getCardNames();
        if (cardNames.size() > 30) {
            return "Error: Maximum of 30 cards allowed";
        }

        List<Card> cards = new ArrayList<>();
        boolean deckCreated = false;
        UserAccount user = null;
        // Check if the user exists in the database
        Optional<UserAccount> userOptional = userAccountRepository.findById(request.getUserID());
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Iterate through the card names and check if they exist in the database
            for (String cardName : cardNames) {
                try {
                    Card card = cardRepository.findByName(cardName).orElseThrow(() -> new Exception("Card not found: " + cardName));
                    cards.add(card);
                } catch (Exception e) {
                    System.out.println("Card not found: " + cardName);
                }
            }
        }

        // Create and save the deck if all cards were found
        if (!cards.isEmpty()) {
            Deck deck = new Deck();
            deck.setUser(user); // Verwendung des 'user'-Objekts, das außerhalb des 'if'-Blocks deklariert wurde
            deck.setName(request.getName());
            deck.setCards(cards);

            deckRepository.save(deck);
            deckCreated = true;
        }

        // Constructing the final message
        if (deckCreated) {
            return "Deck created successfully";
        } else {
            return "Error: Deck could not be created";
        }
    }


    public String updateDeckName(String oldName, String newName) {
        try {
            Optional<Deck> optionalDeck = deckRepository.findByName(oldName);
            if (optionalDeck.isPresent()) {
                Deck deck = optionalDeck.get();
                deck.setName(newName);
                deckRepository.save(deck);
                return "Der Name des Decks wurde erfolgreich aktualisiert.";
            } else {
                throw new RuntimeException("Das Deck wurde nicht gefunden.");
            }
        } catch (RuntimeException e) {
            return "Fehler beim Aktualisieren des Decknamens";
        }
    }






    public String removeCards(String deckName, List<String> cardNamesToRemove) {
        try {
            // Finde das Deck anhand des Namens
            Optional<Deck> optionalDeck = deckRepository.findByName(deckName);
            if (optionalDeck.isPresent()) {
                Deck deck = optionalDeck.get();


                // Extrahiere die IDs der Karten, die entfernt werden sollen
                List<Long> cardIdsToRemove = new ArrayList<>();
                for (String cardName : cardNamesToRemove) {
                    Optional<Card> optionalCard = cardRepository.findByName(cardName);
                    optionalCard.ifPresent(card -> {
                        cardIdsToRemove.add(card.getId());
                        System.out.println("Card ID to remove: " + card.getId()); // Protokollausgabe hinzufügen
                    });
                }

                // Entferne die Karten aus dem Deck über die benannte Abfrage
                deckRepository.deleteDeckCardsByDeckIdAndCardIds(deck.getId(), cardIdsToRemove);

                // Aktualisiere die Liste der Karten im Deck
                deck.getCards().removeIf(card -> cardIdsToRemove.contains(card.getId()));
                deckRepository.save(deck);

                return "Die Karten wurden erfolgreich aus dem Deck entfernt.";
            } else {
                throw new RuntimeException("Das Deck wurde nicht gefunden.");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Entfernen der Karten aus dem Deck: " + e.getMessage()); // Protokollausgabe hinzufügen
            e.printStackTrace(); // Stack-Trace ausgeben
            return "Fehler beim Entfernen der Karten aus dem Deck";
        }
    }






   /* public String addCardsToDeck(String deckName, List<Card> cardsToAdd) {
        try {
            // Überprüfe, ob alle hinzuzufügenden Karten bereits in der Datenbank vorhanden sind
            List<Card> existingCards = new ArrayList<>();
            for (Card card : cardsToAdd) {
                Optional<Card> optionalCard = cardRepository.findByName(card.getName());
                if (optionalCard.isPresent()) {
                    existingCards.add(optionalCard.get());
                } else {
                    throw new RuntimeException("Die Karte '" + card.getName() + "' ist nicht in der Datenbank vorhanden.");
                }
            }

            // Überprüfe, ob das Deck bereits existiert
            Optional<Deck> optionalDeck = deckRepository.findByName(deckName);
            if (optionalDeck.isPresent()) {
                Deck deck = optionalDeck.get();
                List<Card> deckCards = deck.getCards();

                // Überprüfe, ob das Deck die maximale Anzahl von Karten erreicht hat
                if (deckCards.size() + existingCards.size() <= 30) {
                    // Füge die Karten dem Deck hinzu
                    deckCards.addAll(existingCards);

                    // Speichere das aktualisierte Deck in der Datenbank
                    deckRepository.save(deck);
                    return "Die Karten wurden erfolgreich dem Deck hinzugefügt.";
                } else {
                    throw new RuntimeException("Das Deck kann maximal 30 Karten enthalten.");
                }
            } else {
                throw new RuntimeException("Das Deck wurde nicht gefunden.");
            }
        } catch (Exception e) {
            return "Fehler beim Hinzufügen der Karten zum Deck";
        }
    }*/
   public String addCardsToDeck(String deckName, List<String> cardNames) {
       try {
           // Überprüfe, ob das Deck bereits existiert
           Optional<Deck> optionalDeck = deckRepository.findByName(deckName);
           if (optionalDeck.isPresent()) {
               Deck deck = optionalDeck.get();
               List<Card> deckCards = deck.getCards();
               List<Card> existingCards = new ArrayList<>();

               // Überprüfe, ob das Deck die maximale Anzahl von Karten erreicht hat
               if (deckCards.size() + cardNames.size() <= 30) {
                   // Überprüfe, ob alle hinzuzufügenden Karten bereits in der Datenbank vorhanden sind
                   for (String cardName : cardNames) {
                       Optional<Card> optionalCard = cardRepository.findByName(cardName);
                       if (optionalCard.isPresent()) {
                           existingCards.add(optionalCard.get());
                       } else {
                           throw new RuntimeException("Die Karte '" + cardName + "' ist nicht in der Datenbank vorhanden.");
                       }
                   }

                   // Füge die Karten dem Deck hinzu
                   deckCards.addAll(existingCards);

                   // Speichere das aktualisierte Deck in der Datenbank
                   deckRepository.save(deck);
                   return "Die Karten wurden erfolgreich dem Deck hinzugefügt.";
               } else {
                   throw new RuntimeException("Das Deck kann maximal 30 Karten enthalten.");
               }
           } else {
               throw new RuntimeException("Das Deck wurde nicht gefunden.");
           }
       } catch (Exception e) {
           return "Fehler beim Hinzufügen der Karten zum Deck";
       }
   }




    public String replaceCardsInDeck(String deckName, Long userID, List<String> cardsToRemove, List<String> cardsToAdd) {
        try {
            // Überprüfe, ob der Benutzer existiert
            Optional<UserAccount> optionalUser = userAccountRepository.findById(userID);
            if (!optionalUser.isPresent()) {
                throw new RuntimeException("Der Benutzer mit der ID '" + userID + "' wurde nicht gefunden.");
            }

            UserAccount user = optionalUser.get();

            // Überprüfe, ob das Deck dem Benutzer gehört
            Optional<Deck> optionalDeck = deckRepository.findByNameAndUser(deckName, user);
            if (!optionalDeck.isPresent()) {
                throw new RuntimeException("Das Deck mit dem Namen '" + deckName + "' gehört nicht dem Benutzer mit der ID '" + userID + "'.");
            }

            Deck deck = optionalDeck.get();
            List<Card> deckCards = deck.getCards();

            // Überprüfe, ob alle zu entfernenden Karten im Deck vorhanden sind
            List<Card> cardsToRemoveEntities = new ArrayList<>();
            for (String cardName : cardsToRemove) {
                Optional<Card> optionalCard = cardRepository.findByName(cardName);
                if (optionalCard.isPresent()) {
                    cardsToRemoveEntities.add(optionalCard.get());
                } else {
                    throw new RuntimeException("Die Karte '" + cardName + "' ist nicht in der Datenbank vorhanden.");
                }
            }
            if (!deckCards.containsAll(cardsToRemoveEntities)) {
                throw new RuntimeException("Nicht alle Karten, die ersetzt werden sollen, sind im Deck vorhanden.");
            }

            // Überprüfe, ob alle hinzuzufügenden Karten bereits in der Datenbank vorhanden sind
            List<Card> cardsToAddEntities = new ArrayList<>();
            for (String cardName : cardsToAdd) {
                Optional<Card> optionalCard = cardRepository.findByName(cardName);
                if (optionalCard.isPresent()) {
                    cardsToAddEntities.add(optionalCard.get());
                } else {
                    throw new RuntimeException("Die Karte '" + cardName + "' ist nicht in der Datenbank vorhanden.");
                }
            }

            // Überprüfe, ob die Anzahl der zu entfernenden und hinzuzufügenden Karten gleich ist
            if (cardsToRemove.size() != cardsToAdd.size()) {
                throw new RuntimeException("Die Anzahl der zu entfernenden und hinzuzufügenden Karten muss gleich sein.");
            }

            // Entferne die zu entfernenden Karten aus dem Deck
            deckCards.removeAll(cardsToRemoveEntities);

            // Füge die hinzuzufügenden Karten dem Deck hinzu
            if (deckCards.size() + cardsToAddEntities.size() <= 30) {
                deckCards.addAll(cardsToAddEntities);
                // Speichere das aktualisierte Deck in der Datenbank
                deckRepository.save(deck);
                return "Die Karten im Deck wurden erfolgreich ersetzt.";
            } else {
                throw new RuntimeException("Das Deck kann maximal " + 30 + " Karten enthalten.");
            }
        } catch (Exception e) {
            return "Fehler beim Ersetzen der Karten im Deck";
        }
    }




    public List<Card> getAllCardsFromDeck(Long userId, String deckName) {
        try {
            // Überprüfe, ob der Benutzer existiert
            UserAccount user = userAccountRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden."));

            // Überprüfe, ob das Deck dem Benutzer gehört
            Optional<Deck> optionalDeck = deckRepository.findByNameAndUser(deckName, user);
            if (optionalDeck.isPresent()) {
                Deck deck = optionalDeck.get();
                return deck.getCards();
            } else {
                throw new RuntimeException("Das angegebene Deck gehört nicht dem Benutzer.");
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Abrufen der Karten aus dem Deck: " + e.getMessage());
            return Collections.emptyList(); // oder eine andere sinnvolle Aktion, z. B. eine leere Liste zurückgeben
        }
    }






    public List<Deck> getUserDecksByUserId(Long userId) {
        try {
            if (userId != null) {
                List<Deck> userDecks = deckRepository.findByUserId(userId);
                // Entferne die Benutzerinformationen aus den Decks
                for (Deck deck : userDecks) {
                    deck.setUser(null);
                }
                return userDecks;
            } else {
                // Handle case when user ID is not valid
                return Collections.emptyList();
            }
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace();
            return Collections.emptyList();
        }
    }



    @Transactional
    public String deleteDeckByUserIdAndDeckName(Long userId, String deckName) {
        try {
            Optional<UserAccount> userOptional = userAccountRepository.findById(userId);
            if (userOptional.isPresent()) {
                UserAccount user = userOptional.get();
                Optional<Deck> optionalDeck = deckRepository.findByNameAndUser(deckName, user);
                if (optionalDeck.isPresent()) {
                    Deck deck = optionalDeck.get();
                    // Remove the connections between the deck and the cards
                    deck.setCards(Collections.emptyList());
                    deckRepository.save(deck); // Save the changes to update the deck in the database
                    deckRepository.delete(deck); // Delete the deck
                    return "Deck erfolgreich gelöscht";
                } else {
                    return "Das angegebene Deck wurde nicht gefunden.";
                }
            } else {
                return "Der angegebene Benutzer wurde nicht gefunden.";
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Löschen des Decks: " + e.getMessage());
            e.printStackTrace();
            return "Fehler beim Löschen des Decks";
        }
    }
}






