package com.example.demo.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StatisticRepository extends JpaRepository<Statistic, Long> {
    @Query(value = "SELECT * FROM statistic WHERE user1 = ?1 OR user2 = ?1", nativeQuery = true)
    List<Statistic> findByUsername(String username);
}
