package com.example.demo.turnier;

import com.example.demo.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findByBetOn(UserAccount betOn);
}
