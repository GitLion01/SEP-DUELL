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
import java.util.*;
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



        // Check if the user already has 3 decks
        int userDeckCount = deckRepository.countByUserId(request.getUserID());
        if (userDeckCount >= 3) {
            return "Error: Maximum of 3 decks per user allowed";
        }

        // Check if a deck with the same name already exists for this user
        if (!isDeckNameAvailableForUser(request.getName(), request.getUserID())) {
            return "Error: A deck with the name '" + request.getName() + "' already exists for this user";
        }


        // check if deck with this name alredy exists
       /* String deckName = request.getName();
        if (!isDeckNameAvailable(deckName)) {
            return "Error: A deck with the name '" + deckName + "' already exists";
        }*/

        List<String> cardNames = request.getCardNames();
        if (cardNames.size() > 30) {
            return "Error: Maximum of 30 cards allowed";
        }

        List<Card> cards = new ArrayList<>();
        boolean deckCreated = false;
        UserAccount user = null; // Deklaration außerhalb des 'if'-Blocks
        // Check if the user exists in the database
        Optional<UserAccount> userOptional = userAccountRepository.findById(request.getUserID());
        if (userOptional.isPresent()) {
            user = userOptional.get(); // Zuweisung innerhalb des 'if'-Blocks
            // Iterate through the card names and check if they exist in the database
            for (String cardName : cardNames) {
                try {
                    Card card = cardRepository.findByName(cardName).orElseThrow(() -> new Exception("Card not found: " + cardName));
                    cards.add(card);
                } catch (Exception e) {
                    System.out.println("Card not found: " + cardName);
                }
            }

            Deck deck = new Deck();
            deck.setUser(user); // Verwendung des 'user'-Objekts, das außerhalb des 'if'-Blocks deklariert wurde
            deck.setName(request.getName());
            if(!cards.isEmpty()) {
                deck.setCards(cards);
            }
            deckRepository.save(deck);
            deckCreated = true;
        }

       /* // Create and save the deck if all cards were found
        if (!cards.isEmpty()) {
            Deck deck = new Deck();
            deck.setUser(user); // Verwendung des 'user'-Objekts, das außerhalb des 'if'-Blocks deklariert wurde
            deck.setName(request.getName());
            deck.setCards(cards);
        }

            deckRepository.save(deck);
            deckCreated = true;
        }*/




        // Constructing the final message
        if (deckCreated) {
            return "Deck created successfully";
        } else {
            return "Error: Deck could not be created";
        }
    }

    // Method to check if a deck name is available for a given user
    private boolean isDeckNameAvailableForUser(String deckName, Long userId) {
        Optional<Deck> existingDeck = deckRepository.findByNameAndUserId(deckName, userId);
        return existingDeck.isEmpty();
    }


   /* public String updateDeckName(String oldName, String newName) {
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
    }*/

    
    public String updateDeckName(Long userId, String oldName, String newName) {
        try {
            // Überprüfen, ob der Benutzer existiert
            Optional<UserAccount> optionalUser = userAccountRepository.findById(userId);
            if (optionalUser.isPresent()) {
                // Überprüfen, ob das Deck existiert und dem Benutzer gehört
                Optional<Deck> optionalOldDeck = deckRepository.findByNameAndUserId(oldName, userId);
                if (optionalOldDeck.isPresent()) {
                    // Überprüfen, ob der neue Name bereits für ein anderes Deck dieses Benutzers verwendet wird
                    Optional<Deck> optionalNewDeck = deckRepository.findByNameAndUserId(newName, userId);
                    if (optionalNewDeck.isPresent()) {
                        return "Ein Deck mit dem Namen '" + newName + "' existiert bereits.";
                    } else {
                        // Aktualisieren Sie den Decknamen
                        Deck deck = optionalOldDeck.get();
                        deck.setName(newName);
                        deckRepository.save(deck);
                        return "Deck gefunden und Name erfolgreich aktualisiert";
                    }
                } else {
                    return "Deck nicht gefunden";
                }
            } else {
                return "Benutzer nicht gefunden";
            }
        } catch (Exception e) {
            return "Fehler beim Aktualisieren des Decknamens: " + e.getMessage();
        }
    }






    /*public String removeCards(String deckName, List<String> cardNamesToRemove) {
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
    }*/

    public String removeAllCardsInstancesFromDeck(DeckRequest request) {
        try {
            // Überprüfe, ob der Benutzer existiert
            Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserID());
            if (!optionalUser.isPresent()) {
                throw new RuntimeException("Der Benutzer mit der angegebenen ID wurde nicht gefunden.");
            }

            // Finde das Deck anhand des Namens und der UserID
            Optional<Deck> optionalDeck = deckRepository.findByNameAndUserId(request.getName(), request.getUserID());
            if (optionalDeck.isPresent()) {
                Deck deck = optionalDeck.get();

                // Extrahiere die IDs der Karten, die entfernt werden sollen
                List<Long> cardIdsToRemove = new ArrayList<>();
                for (String cardName : request.getCardNames()) {
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
            e.getMessage(); // Stack-Trace ausgeben
            return "Fehler beim Entfernen der Karten aus dem Deck: " + e.getMessage();
        }
    }

    /*public String removeFirstInstanceOfCardType(DeckRequest request) {
        try {
            // Überprüfe, ob der Benutzer existiert
            Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserID());
            if (!optionalUser.isPresent()) {
                throw new RuntimeException("Der Benutzer mit der angegebenen ID wurde nicht gefunden.");
            }

            // Finde das Deck anhand des Namens und der UserID
            Optional<Deck> optionalDeck = deckRepository.findByNameAndUserId(request.getName(), request.getUserID());
            if (optionalDeck.isPresent()) {
                Deck deck = optionalDeck.get();
                List<String> cardNamesToRemove = request.getCardNames();

                // Durchlaufe die Liste der Karten im Deck
                Iterator<Card> iterator = deck.getCards().iterator();
                while (iterator.hasNext()) {
                    Card card = iterator.next();
                    if (cardNamesToRemove.contains(card.getName())) {
                        // Entferne das erste Vorkommen der Karte aus dem Deck
                        iterator.remove();
                        // Entferne die Karte aus der Liste der zu entfernenden Karten
                        cardNamesToRemove.remove(card.getName());
                        break; // Nur das erste Vorkommen entfernen
                    }
                }

                // Aktualisiere das Deck in der Datenbank
                deckRepository.save(deck);

                return "Das erste Vorkommen der Karte(n) wurde(n) erfolgreich aus dem Deck entfernt.";
            } else {
                throw new RuntimeException("Das Deck wurde nicht gefunden.");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Entfernen der Karte(n) aus dem Deck: " + e.getMessage());
            return "Fehler beim Entfernen der Karte(n) aus dem Deck: " + e.getMessage();
        }
    }*/

    public String removeFirstInstanceOfCardType(DeckRequest request) {
        try {
            // Überprüfe, ob der Benutzer existiert
            Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserID());
            if (!optionalUser.isPresent()) {
                throw new RuntimeException("Der Benutzer mit der angegebenen ID wurde nicht gefunden.");
            }

            // Finde das Deck anhand des Namens und der UserID
            Optional<Deck> optionalDeck = deckRepository.findByNameAndUserId(request.getName(), request.getUserID());
            if (optionalDeck.isPresent()) {
                Deck deck = optionalDeck.get();
                List<String> cardNamesToRemove = request.getCardNames();
                List<String> deletedCardNames = new ArrayList<>();

                for (String cardName : cardNamesToRemove) {
                    // Überprüfen, ob die Karte im Deck vorhanden ist
                    boolean cardFound = false;
                    for (Card card : deck.getCards()) {
                        if (card.getName().equals(cardName)) {
                            deck.getCards().remove(card);
                            deletedCardNames.add(cardName);
                            cardFound = true;
                            break;
                        }
                    }
                    if (!cardFound) {
                        deletedCardNames.add(cardName + " (nicht gefunden)");
                    }
                }

                // Aktualisiere das Deck in der Datenbank
                deckRepository.save(deck);

                if (!deletedCardNames.isEmpty()) {
                    return "Die erste Instanz der Karte(n) " + deletedCardNames + " wurde(n) erfolgreich aus dem Deck entfernt.";
                } else {
                    return "Keine Karten wurden aus dem Deck entfernt.";
                }
            } else {
                throw new RuntimeException("Das Deck wurde nicht gefunden.");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Entfernen der Karte(n) aus dem Deck: " + e.getMessage());
            return "Fehler beim Entfernen der Karte(n) aus dem Deck: " + e.getMessage();
        }
    }









   /*public String addCardsToDeck(DeckRequest request) {
       try {
           // Überprüfe, ob das Deck bereits existiert
           Optional<Deck> optionalDeck = deckRepository.findByName(request.getName());
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
   }*/

   public String addCardsToDeck(DeckRequest request) {
       try {
           // Überprüfen, ob der Benutzer existiert
           Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserID());
           if (optionalUser.isPresent()) {
               UserAccount user = optionalUser.get();

               // Alle Decks des Benutzers abrufen
               /*List<Deck> userDecks = user.getDecks();*/

               // Das passende Deck finden
               /*Optional<Deck> optionalDeck = userDecks.stream()
                       .filter(deck -> deck.getName().equals(request.getName()))
                       .findFirst();*/
               Optional<Deck> optionalDeck = deckRepository.findAllDecksByUserIdAndName(request.getUserID(), request.getName());





               if (optionalDeck.isPresent()) {
                   Deck deck = optionalDeck.get();
                   List<Card> deckCards = deck.getCards();
                   List<Card> existingCards = new ArrayList<>();

                   // Überprüfen, ob das Deck die maximale Anzahl von Karten erreicht hat
                   if (deckCards.size() + request.getCardNames().size() <= 30) {
                       // Überprüfen, ob alle hinzuzufügenden Karten bereits in der Datenbank vorhanden sind
                       for (String cardName : request.getCardNames()) {
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
           } else {
               throw new RuntimeException("Der Benutzer mit der angegebenen ID wurde nicht gefunden.");
           }
       } catch (Exception e) {
           return "Fehler beim Hinzufügen der Karten zum Deck: " + e.getMessage();
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






