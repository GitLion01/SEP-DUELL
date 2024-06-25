package com.example.demo.game;

import com.example.demo.user.UserAccount;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    boolean existsByUsersContaining(UserAccount user);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM game_users WHERE user_id IN (:userIds)", nativeQuery = true)
    void deleteFromGameUsersByUserIds(@Param("userIds") List<Long> userIds);

    /*
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM game WHERE id IN (SELECT game_id FROM game_users WHERE user_id IN (:userIds))", nativeQuery = true)
    void deleteGamesByUserIds(@Param("userIds") List<Long> userIds);*/
}
