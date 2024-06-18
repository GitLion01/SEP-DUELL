package com.example.demo.game;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerStateRepository extends JpaRepository<PlayerState, Long> {
    Optional<PlayerState> findByUserId(Long userID);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM player_state_hand_cards WHERE player_state_id IN (SELECT player_state_id FROM user_account WHERE user_account.id IN (:userIds))", nativeQuery = true)
    void deleteHandCardsByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM player_state_deck_clone WHERE player_state_id IN (SELECT player_state_id FROM user_account WHERE user_account.id IN (:userIds))", nativeQuery = true)
    void deleteDeckCloneByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM player_state_field_cards WHERE player_state_id IN (SELECT player_state_id FROM user_account WHERE user_account.id IN (:userIds))", nativeQuery = true)
    void deleteFieldCardsByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM player_state_cards_played WHERE player_state_id IN (SELECT player_state_id FROM user_account WHERE user_account.id IN (:userIds))", nativeQuery = true)
    void deleteCardsPlayedByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM player_card WHERE player_state_id IN (SELECT player_state_id FROM user_account WHERE user_account.id IN (:userIds))", nativeQuery = true)
    void deletePlayerCardsByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM player_state WHERE player_state.id IN (SELECT player_state_id FROM user_account WHERE user_account.id IN (:userIds))", nativeQuery = true)
    void deletePlayerStatesByUserIds(@Param("userIds") List<Long> userIds);
}
