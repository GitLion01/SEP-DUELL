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

    @Query(value = "SELECT * FROM game where game.streamed = true AND game.ready = true", nativeQuery = true)
    Optional<List<Game>> findAllStreams();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM game_viewers WHERE game_viewers.viewers_id = ?1", nativeQuery = true)
    void deleteFromGameViewersByUserIds(@Param("userId") Long userId);


}
