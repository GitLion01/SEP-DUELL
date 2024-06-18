package com.example.demo.game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PlayerStateRepository extends JpaRepository<PlayerState, Long> {
}
