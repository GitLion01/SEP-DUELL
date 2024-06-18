package com.example.demo.game;

import com.example.demo.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByIdAndId(Long Id, Long userID);

    boolean existsByUsersContaining(UserAccount user);

    @Transactional
    @Query(value = "SELECT game_id FROM game_users where user_id in (:userID)", nativeQuery = true)
    boolean findByUserId(Long userID);

    @Transactional
    @Query(value = "SELECT game_id FROM game_users where user_id in (:userID)", nativeQuery = true)
    Optional<Game> findByUserIdOptional(Long userID);

}
