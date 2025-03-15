package com.example.demo.decks;

import com.example.demo.cards.Card;
import com.example.demo.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {




    @Transactional
    @Modifying
    @Query(value = "DELETE FROM deck_card WHERE deck_id = ?1 AND card_id IN (?2)", nativeQuery = true)
    void deleteDeckCardsByDeckIdAndCardIds(@Param("deckID") Long deckID, @Param("cardIds") List<Long> cardIds);

    List<Deck> findByUserId(Long userId);


    int countByUserId(Long userID);

    Optional<Deck> findByNameAndUser(String deckName, UserAccount user);

    @Query(value = "SELECT * FROM deck WHERE id = :deckId AND user_id = :userId", nativeQuery = true)
    Optional<Deck> findByDeckIdAndUserId(Long deckId, Long userId);

    Optional<Deck> findAllDecksByUserIdAndName(Long userId, String deckName);

    Optional<Deck> findByNameAndUserId(String deckName, Long userId);

    List<Deck> findByCardsContaining(Card card);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM deck_card where deck_id = ?1", nativeQuery = true)
    void deleteDeckCardsByDeckId(@Param("deckID") Long deckID);
}






