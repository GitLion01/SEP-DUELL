// src/main/java/com/example/demo/leaderboard/LeaderboardService.java
package com.example.demo.leaderboard;
import com.example.demo.game.GameRepository;            //für duell Status
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
    private final GameRepository gameRepository;                    //für duell Status

    public List<UserAccount> getLeaderboard() {
        return userAccountRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getLeaderboardPoints().compareTo(u1.getLeaderboardPoints()))
                .collect(Collectors.toList());
    }

    public void updateUserStatus(Long userId, String status) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user is in a game
        // für duell Status von
        if (gameRepository.existsByUsersContaining(user)) {
            //Game game=gameRepository.findByUsersContaining(user).get();
            status = "im Duell";
            //game.getUsers().get(0).setStatus(status);
            //game.getUsers().get(1).setStatus(status);
        }                                  //für duell Status bis

        user.setStatus(status);
        userAccountRepository.save(user);
        messagingTemplate.convertAndSend("/status/leaderboard", user);   ///topic/leaderboard   //user-Objekt = alle user nicht eine einzelne userid
    }
}
