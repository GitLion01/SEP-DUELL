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

    public String deleteCard(CardRequest request)throws IllegalStateException{
        boolean exists = cardRepository.findByName(request.getName()).isPresent();
        if(!exists) {
            throw new IllegalStateException("card does not exists");
        }
        for(Card card : cardRepository.findAll()) {
            if(card.getName().equals(request.getName())) {
                cardRepository.delete(card);
            }
        }
        return "card deleted";
    }

}
