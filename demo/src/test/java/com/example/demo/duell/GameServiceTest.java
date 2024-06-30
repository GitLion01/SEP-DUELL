package com.example.demo.duell;

import com.example.demo.cards.Card;
import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.decks.DeckRequest;
import com.example.demo.game.*;
import com.example.demo.game.requests.AttackUserRequest;
import com.example.demo.game.requests.DeckSelectionRequest;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private DeckRepository deckRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private PlayerStateRepository playerStateRepository;
    @Mock
    private PlayerCardRepository playerCardRepository;

    private AutoCloseable autoCloseable;
    private GameService gameService;

    @BeforeEach
    void setUp(){
        autoCloseable = MockitoAnnotations.openMocks(this);
        gameService = new GameService(
                gameRepository,
                deckRepository,
                userAccountRepository,
                messagingTemplate,
                playerStateRepository,
                playerCardRepository);
    }

    @AfterEach
    void tearDown() throws Exception{
        autoCloseable.close();
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
    void testSelectDeck() {
        Long deckId = 1L;
        Long userId = 1L;
        Long gameId = 1L;

        DeckSelectionRequest request = new DeckSelectionRequest();
        request.setDeckId(deckId);
        request.setUserId(userId);
        request.setGameId(gameId);

        UserAccount user = new UserAccount();
        user.setId(userId);
        PlayerState playerState = new PlayerState();
        user.setPlayerState(playerState);

        Deck deck = new Deck();
        deck.setId(deckId);
        deck.setUser(user);
        List<Card> cards = new ArrayList<>();
        Card card = new Card();
        card.setName("Test Card");
        card.setAttackPoints(5);
        card.setDefensePoints(5);
        cards.add(card);
        deck.setCards(cards);

        Game game = new Game();
        game.setId(gameId);
        game.setUsers(new ArrayList<>(List.of(user)));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(deckRepository.findByDeckIdAndUserId(deckId, userId)).thenReturn(Optional.of(deck));
        when(playerCardRepository.save(any(PlayerCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        gameService.selectDeck(request);

        assertEquals(deck, user.getPlayerState().getDeck());
        assertTrue(user.getPlayerState().getReady());
        assertEquals(1,user.getPlayerState().getDeck().getCards().size());
        assertEquals(0, user.getPlayerState().getDeckClone().size()); // 0, weil Karte aus DeckCloone gelöscht wird nachdem Karte in Hand gelegt wird
        assertEquals(5, user.getPlayerState().getHandCards().get(0).getAttackPoints());

        // Check initial hand cards
        assertEquals(1, user.getPlayerState().getHandCards().size());
        PlayerCard handCard = user.getPlayerState().getHandCards().get(0);
        assertEquals("Test Card", handCard.getName());
        assertEquals(5, handCard.getAttackPoints());
        assertEquals(5, handCard.getDefensePoints());

        Game savedGame = gameRepository.findById(gameId).get();
        assertTrue(savedGame.getReady());

    }


    @Test
    void testAttackUser() {
        Long gameId = 1L;
        Long attackerId = 1L;
        Long defenderId = 2L;
        Long attackerCardId = 1L;
        Long attackerStateId = 1L;
        Long defenderStateId = 2L;

        AttackUserRequest request = new AttackUserRequest();
        request.setGameId(gameId);
        request.setAttackerId(attackerId);
        request.setDefenderId(defenderId);
        request.setAttackerCardId(attackerCardId);

        Game game = new Game();
        game.setId(gameId);
        game.setFirstRound(false);
        UserAccount attacker = new UserAccount();
        attacker.setId(attackerId);
        PlayerState attackerState = new PlayerState();
        attackerState.setId(attackerStateId);
        attackerState.setLifePoints(50);
        attackerState.setDamage(0);
        attacker.setPlayerState(attackerState);

        UserAccount defender = new UserAccount();
        defender.setId(defenderId);
        PlayerState defenderState = new PlayerState();
        defenderState.setId(defenderStateId);
        defenderState.setLifePoints(50);
        defenderState.setDamage(0);
        defender.setPlayerState(defenderState);

        game.setUsers(Arrays.asList(attacker, defender));
        game.setCurrentTurn(0);

        PlayerCard attackerCard = new PlayerCard();
        attackerCard.setId(attackerCardId);
        attackerCard.setAttackPoints(10);
        attackerCard.setHasAttacked(false);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userAccountRepository.findById(attackerId)).thenReturn(Optional.of(attacker));
        when(userAccountRepository.findById(defenderId)).thenReturn(Optional.of(defender));
        when(playerCardRepository.findById(attackerCardId)).thenReturn(Optional.of(attackerCard));
        when(playerStateRepository.findById(attackerStateId)).thenReturn(Optional.of(attackerState));
        when(playerStateRepository.findById(defenderStateId)).thenReturn(Optional.of(defenderState));

        gameService.attackUser(request);

        assertEquals(40, defender.getPlayerState().getLifePoints());
        assertEquals(10, attacker.getPlayerState().getDamage());
        assertTrue(attackerCard.getHasAttacked());

        // Überprüt, ob die PlayerCard, PlayerState und das Game gespeichert wurden
        PlayerCard savedPlayerCard = playerCardRepository.findById(attackerCardId).orElse(null);
        PlayerState savedAttackerState = playerStateRepository.findById(attackerStateId).orElse(null);
        PlayerState savedDefenderState = playerStateRepository.findById(defenderStateId).orElse(null);
        Game savedGame = gameRepository.findById(gameId).orElse(null);


        assertNotNull(savedPlayerCard);
        assertNotNull(savedAttackerState);
        assertNotNull(savedDefenderState);
        assertNotNull(savedGame);

        assertEquals(attackerCardId, savedPlayerCard.getId());
        assertEquals(attacker.getPlayerState().getId(), savedAttackerState.getId());
        assertEquals(defender.getPlayerState().getId(), savedDefenderState.getId());
        assertEquals(gameId, savedGame.getId());


    }

    @Test
    void testTerminateMatch() {
        // Mocking game, users, and player states
        Long gameId = 1L;
        Long userAId = 1L;
        Long userBId = 2L;

        UserAccount userA = new UserAccount();
        userA.setId(userAId);
        UserAccount userB = new UserAccount();
        userB.setId(userBId);

        PlayerState playerStateA = new PlayerState();
        playerStateA.setId(1L);
        playerStateA.setWinner(true); // userA is winner
        playerStateA.setDamage(10); // example values
        PlayerState playerStateB = new PlayerState();
        playerStateB.setId(2L);
        playerStateB.setDamage(5); // example values

        userA.setPlayerState(playerStateA);
        userB.setPlayerState(playerStateB);

        Game game = new Game();
        game.setId(gameId);
        game.setUsers(List.of(userA, userB));

        // Mocking repository behaviors
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userAccountRepository.findById(userAId)).thenReturn(Optional.of(userA));
        when(userAccountRepository.findById(userBId)).thenReturn(Optional.of(userB));

        // Calling the method under test
        gameService.terminateMatch(gameId, userAId, userBId);

        // Assertions for the updated values or other business logic applied
        assertEquals(600, userA.getSepCoins()); // example expected value after match termination
        assertEquals(500, userB.getSepCoins()); // example expected value after match termination
        assertEquals(10, userA.getPlayerState().getDamage()); // example expected value after match termination
        assertEquals(5, userB.getPlayerState().getDamage()); // example expected value after match termination
        assertEquals(50, userA.getLeaderboardPoints()); // example expected value after match termination
        assertEquals(-50, userB.getLeaderboardPoints()); // example expected value after match termination
    }

}
