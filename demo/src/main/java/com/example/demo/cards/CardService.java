package com.example.demo.cards;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public String addCard(CardRequest request) throws IllegalStateException{
        boolean cardExists = cardRepository.findByName(request.getName()).isPresent();

        if(cardExists) {
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



}
