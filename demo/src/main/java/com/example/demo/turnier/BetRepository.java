package com.example.demo.turnier;

import com.example.demo.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {   // Bet=Entität, Long = Id Primärschlüssel / Typensicherheit, Wiederverwendbarkeit und Klarheit
    List<Bet> findByBetOn(UserAccount betOn);   // Liste mit allen Wetten (Bet-Objekten) auf einen bestimmten User
}
