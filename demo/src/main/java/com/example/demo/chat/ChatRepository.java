package com.example.demo.chat;

import com.example.demo.decks.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
