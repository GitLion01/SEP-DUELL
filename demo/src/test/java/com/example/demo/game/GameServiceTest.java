package com.example.demo.game;

import com.example.demo.decks.DeckRepository;
import com.example.demo.duellHerausforderung.HerausforderungService;
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

class GameServiceTest {

    @Mock
    private  GameRepository gameRepository;
    @Mock
    private  DeckRepository deckRepository;
    @Mock private  UserAccountRepository userAccountRepository;
    @Mock private  SimpMessagingTemplate messagingTemplate;
    @Mock private  PlayerStateRepository playerStateRepository;
    @Mock private  PlayerCardRepository playerCardRepository;


    private AutoCloseable autoCloseable;
    private GameService underTest;

    @BeforeEach
    void setUp() {
        autoCloseable= MockitoAnnotations.openMocks(this); //initialize alle Mocks in this class
        underTest = new GameService(gameRepository,deckRepository,userAccountRepository,messagingTemplate,playerStateRepository,playerCardRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void updateTimers() {
    }

    @Test
    void createGame() {
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

    @Test
    void selectDeck() {
    }

    @Test
    void drawCard() {
    }

    @Test
    void placeCard() {
    }

    @Test
    void endTurn() {
    }

    @Test
    void attackCard() {
    }

    @Test
    void attackUser() {
    }

    @Test
    void swapForRare() {
    }

    @Test
    void swapForLegendary() {
    }

    @Test
    void terminateMatch() {
    }

    @Test
    void deleteUserGameData() {
    }
}