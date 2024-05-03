package com.example.demo.cards;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public String addCard(CardRequest request) throws IllegalStateException{
        boolean cardExists = cardRepository.findByName(request.getName()).isEmpty();

        if(!cardExists) {
            throw new IllegalStateException("card already exists");
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

    public String deleteCard(String name) throws IllegalStateException {
        Optional<Card> optionalCard = cardRepository.findByName(name);
        if (optionalCard.isEmpty()) {
            throw new IllegalStateException("Card does not exist");
        }
        Card card = optionalCard.get();
        cardRepository.delete(card);
        return "Card deleted";
    }


}
