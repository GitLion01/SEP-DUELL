package com.example.demo.leaderboard;

import com.example.demo.chat.ChatService;
import com.example.demo.game.GameRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class LeaderboardServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private GameRepository gameRepository;

    LeaderboardService underTest;
    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable= MockitoAnnotations.openMocks(this); //initialize alle Mocks in this class
        underTest = new LeaderboardService(userAccountRepository,messagingTemplate,gameRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void getLeaderboard() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        UserAccount user1=new UserAccount();
        user1.setId(userId1);
        user1.setLeaderboardPoints(100);
        UserAccount user2=new UserAccount();
        user2.setId(userId2);
        user2.setLeaderboardPoints(50);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user2));
        List<UserAccount> leaderboard =new ArrayList<>();

        if(userAccountRepository.findById(1L).isPresent() && userAccountRepository.findById(2L).isPresent()){
            leaderboard.add(userAccountRepository.findById(1L).get());
            leaderboard.add(userAccountRepository.findById(2L).get());
        }

    }

    @Test
    void updateUserStatus() {
    }
}