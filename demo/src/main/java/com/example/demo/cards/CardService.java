package com.example.demo.cards;


import com.example.demo.user.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public String addCard(CardRequest request){
        Optional<Card> cardExists = cardRepository.findByName(request.getName());

        if(cardExists.isPresent()) {
            return "card already exists";
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
        return "card added";
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

}
