// src/main/java/com/example/demo/leaderboard/LeaderboardController.java
package com.example.demo.leaderboard;

import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    @GetMapping("/leaderboard")
    public List<UserAccount> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }

    @MessageMapping("/status")
    public void updateUserStatus(@Header("userId") String userIdHeader, @Payload String status) {
        Long userId =  Long.parseLong(userIdHeader);
        System.out.println("aufgerufen");
        leaderboardService.updateUserStatus(userId, status);
    }
}
