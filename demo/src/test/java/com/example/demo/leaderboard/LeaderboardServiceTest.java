package com.example.demo.leaderboard;

import com.example.demo.chat.ChatService;
import com.example.demo.game.Game;
import com.example.demo.game.GameRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class LeaderboardServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private GameRepository gameRepository;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable= MockitoAnnotations.openMocks(this); //initialize alle Mocks in this class
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
        UserAccount[] leaderboard;
        if(userAccountRepository.findById(1L).isPresent() && userAccountRepository.findById(2L).isPresent()){
            leaderboard = new UserAccount[]{userAccountRepository.findById(1L).get(),userAccountRepository.findById(2L).get()};
            Arrays.sort(leaderboard, Comparator.comparing(UserAccount::getLeaderboardPoints));
        }
        else
            leaderboard = new UserAccount[0];

        assertEquals(leaderboard.length, 2);
        assertTrue(isSorted(leaderboard));
    }

    private boolean isSorted(UserAccount[] leaderboard) {
        for (int i = 0; i < leaderboard.length - 1; i++) {
            if (leaderboard[i].getLeaderboardPoints() > leaderboard[i + 1].getLeaderboardPoints()) {
                return false;
            }
        }
        return true;
    }

    @Test
    void updateUserStatus() {
        Long userId1 = 1L;
        UserAccount user1=new UserAccount();
        user1.setId(userId1);
        user1.setStatus("im Duell");

        Game game=new Game();
        game.setId(1L);
        game.getUsers().add(user1);

        Game gameToTest=new Game();
        UserAccount userToTest=new UserAccount();
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        if(gameRepository.findById(1L).isPresent()){
            gameToTest = gameRepository.findById(1L).get();
            userToTest = gameToTest.getUsers().get(0);
        }
        assertEquals(userToTest.getStatus(),"im Duell");
    }
}