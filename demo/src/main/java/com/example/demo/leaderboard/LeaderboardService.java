// src/main/java/com/example/demo/leaderboard/LeaderboardService.java
package com.example.demo.leaderboard;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LeaderboardService {
    private final UserAccountRepository userAccountRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<UserAccount> getLeaderboard() {
        return userAccountRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getLeaderboardPoints().compareTo(u1.getLeaderboardPoints()))
                .collect(Collectors.toList());
    }

    public void updateUserStatus(Long userId, String status) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setStatus(status);
        userAccountRepository.save(user);
        messagingTemplate.convertAndSend("/topic/leaderboard", user);   ///topic/leaderboard   //user-Objekt = alle user nicht eine einzelne userid
    }
}
