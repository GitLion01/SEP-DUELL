// src/main/java/com/example/demo/leaderboard/LeaderboardController.java
package com.example.demo.leaderboard;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import com.example.demo.user.UserAccountService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class LeaderboardController {
    private final LeaderboardService leaderboardService;
    private final UserAccountRepository userAccountRepository;

    @GetMapping("/leaderboard")
    public List<UserAccount> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }

    @MessageMapping("/status")
    public void updateUserStatus(@Header("userId") String userIdHeader, @Payload String status) {
        boolean isID=false;
        Long userId=0L;
        try {
            userId = Long.parseLong(userIdHeader);
            isID=true;
            System.out.println("from the try");
        }
        catch (NumberFormatException e) {
            System.out.println("User Name ist gegeben");
        }
        if(!isID)
        {
            System.out.println("from the if");
            userId=userAccountRepository.findByUsername(userIdHeader).get().getId();
        }
        leaderboardService.updateUserStatus(userId, status);
    }

    @GetMapping("/getId/{senderName}")
    public Long getId(@PathVariable("senderName") String senderName) {
        return userAccountRepository.findByUsername(senderName).get().getId();
    }
}
