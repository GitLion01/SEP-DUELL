// src/main/java/com/example/demo/leaderboard/LeaderboardController.java
package com.example.demo.leaderboard;

import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/leaderboard/status")
    public void updateUserStatus(@RequestParam Long userId, @RequestParam String status) {
        leaderboardService.updateUserStatus(userId, status);
    }
}
