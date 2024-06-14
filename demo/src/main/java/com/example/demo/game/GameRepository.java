package com.example.demo.game;

import com.example.demo.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByidAndId(Long Id, Long userID);

    boolean existsByUsersContaining(UserAccount user);


}
