package com.example.demo.duellHerausforderung;

import com.example.demo.game.Game;
import com.example.demo.game.GameRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class HerausforderungServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private GameRepository gameRepository;

    private AutoCloseable autoCloseable;
    private HerausforderungService underTest;


    @BeforeEach
    void setUp() {
        autoCloseable= MockitoAnnotations.openMocks(this); //initialize alle Mocks in this class
        underTest = new HerausforderungService(messagingTemplate,userAccountRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }


    @Test
    void toDuell() {
        Long senderId= 1L;
        Long receiverId= 2L;
        UserAccount userAccount1=new UserAccount();
        userAccount1.setId(senderId);
        UserAccount userAccount2=new UserAccount();
        userAccount2.setId(receiverId);

        when(userAccountRepository.findById(senderId)).thenReturn(Optional.of(userAccount1));
        when(userAccountRepository.findById(receiverId)).thenReturn(Optional.of(userAccount2));

        Game game=new Game();
        game.setId(1L);
        game.getUsers().add(userAccount1);
        game.getUsers().add(userAccount2);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        ResponseEntity<Game> testGame ;
        if(gameRepository.findById(1L).get().getUsers().size()==2)
            testGame = new ResponseEntity<>(gameRepository.findById(1L).get(), HttpStatus.OK);
        else
            testGame = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        assertEquals(HttpStatus.OK,testGame.getStatusCode());
    }
}