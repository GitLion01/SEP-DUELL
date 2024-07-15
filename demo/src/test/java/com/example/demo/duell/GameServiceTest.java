package com.example.demo.duell;

import com.example.demo.cards.Card;
import com.example.demo.cards.Rarity;
import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.game.*;
import com.example.demo.game.requests.*;
import com.example.demo.leaderboard.LeaderboardService;
import com.example.demo.turnier.TurnierService;
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
    @Mock
    private LeaderboardService leaderboardService;
    @Mock
    private StatisticRepository statisticRepository;
    @Mock
    private TurnierService turnierService;

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
                playerCardRepository,
                leaderboardService,
                statisticRepository,
                turnierService
        );
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
        Long pSId = 1L;

        DeckSelectionRequest request = new DeckSelectionRequest();
        request.setDeckId(deckId);
        request.setUserId(userId);
        request.setGameId(gameId);

        UserAccount user = new UserAccount();
        user.setId(userId);
        PlayerState playerState = new PlayerState();
        playerState.setId(pSId);
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
        user.setDecks(List.of(deck));

        Game game = new Game();
        game.setId(gameId);
        game.setUsers(new ArrayList<>(List.of(user)));
        game.setCurrentTurn(user);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(deckRepository.findByDeckIdAndUserId(deckId, userId)).thenReturn(Optional.of(deck));
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerStateRepository.findById(pSId)).thenReturn(Optional.of(playerState));
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
        game.setCurrentTurn(attacker);

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

        Long gameId = 1L;
        Long userAId = 1L;
        Long userBId = 2L;

        UserAccount userA = new UserAccount();
        userA.setId(userAId);
        UserAccount userB = new UserAccount();
        userB.setId(userBId);

        PlayerState playerStateA = new PlayerState();
        playerStateA.setId(1L);
        playerStateA.setWinner(true);
        playerStateA.setDamage(10);
        PlayerState playerStateB = new PlayerState();
        playerStateB.setId(2L);
        playerStateB.setDamage(5);

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
        assertEquals(600, userA.getSepCoins());
        assertEquals(500, userB.getSepCoins());
        assertEquals(50, userA.getLeaderboardPoints());
        assertEquals(-50, userB.getLeaderboardPoints());
    }



    @Test
    void testEndTurnWithBot() {
        Long gameId = 1L;
        Long userId = 1L;
        Long botStateId = 2L;

        EndTurnRequest request = new EndTurnRequest();
        request.setGameID(gameId);
        request.setUserID(userId);

        UserAccount user = new UserAccount();
        user.setId(userId);
        PlayerState userPlayerState = new PlayerState();
        userPlayerState.setId(1L);
        userPlayerState.setLifePoints(20);
        user.setPlayerState(userPlayerState);

        PlayerState botPlayerState = new PlayerState();
        botPlayerState.setId(botStateId);
        botPlayerState.setLifePoints(20);

        // Create a PlayerCard for the bot's deck clone and hand
        PlayerCard botCard = new PlayerCard();
        botCard.setName("Bot Card");
        botCard.setAttackPoints(5);
        botCard.setDefensePoints(5);
        botCard.setRarity(Rarity.NORMAL);
        botCard.setPlayerState(botPlayerState);

        botPlayerState.setDeckClone(new ArrayList<>(List.of(botCard)));
        botPlayerState.setHandCards(new ArrayList<>());
        botPlayerState.setFieldCards(new ArrayList<>());

        // Create a PlayerCard for the user's deck clone and hand
        PlayerCard userCard = new PlayerCard();
        userCard.setName("User Card");
        userCard.setAttackPoints(3);
        userCard.setDefensePoints(3);
        userCard.setPlayerState(userPlayerState);

        userPlayerState.setDeckClone(new ArrayList<>(List.of(userCard)));
        userPlayerState.setHandCards(new ArrayList<>());
        userPlayerState.setFieldCards(new ArrayList<>());

        Game game = new Game();
        game.setId(gameId);
        game.setUsers(List.of(user));
        game.setPlayerStateBot(botPlayerState);
        game.setCurrentTurn(user);
        game.setMyTurn(true);
        game.setFirstRound(true);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerStateRepository.findById(botStateId)).thenReturn(Optional.of(botPlayerState));
        when(playerStateRepository.findById(userPlayerState.getId())).thenReturn(Optional.of(userPlayerState));
        when(playerCardRepository.save(any(PlayerCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playerStateRepository.save(any(PlayerState.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        gameService.endTurn(request);

        // Assertions for user player state
        assertEquals(1, user.getPlayerState().getHandCards().size());
        assertEquals("User Card", user.getPlayerState().getHandCards().get(0).getName());
        assertEquals(0, user.getPlayerState().getDeckClone().size());

        // Assertions for bot player state
        assertEquals(0, botPlayerState.getHandCards().size());
        assertEquals(0, botPlayerState.getDeckClone().size());
        assertEquals(1, botPlayerState.getFieldCards().size());
        assertEquals("Bot Card", botPlayerState.getFieldCards().get(0).getName());

        // Assertions for game state
        assertFalse(game.getFirstRound());
        assertTrue(game.getMyTurn());
        assertEquals(user, game.getCurrentTurn());

        // Verify messaging
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq(user.getId().toString()), eq("/queue/game"), any());
    }


    @Test
    void testAttackBotCard() {
        // IDs für Testinstanzen initialisieren
        Long gameId = 1L;
        Long attackerId = 1L;
        Long botPSId = 2L;
        Long attackerCardId = 1L;
        Long targetCardId = 2L;

        //Request-Objekt initialisieren
        AttackBotCardRequest request = new AttackBotCardRequest();
        request.setGameId(gameId);
        request.setUserIdAttacker(attackerId);
        request.setBotPSId(botPSId);
        request.setAttackerId(attackerCardId);
        request.setTargetId(targetCardId);

        // UserAccount und zugehörigen PlayerState initialisieren
        UserAccount attacker = new UserAccount();
        attacker.setId(attackerId);
        PlayerState attackerState = new PlayerState();
        attacker.setPlayerState(attackerState);

        //Bot PlayerState initialisieren
        PlayerState botPS = new PlayerState();
        botPS.setId(botPSId);

        //Angreifende Karte initialisieren
        PlayerCard attackerCard = new PlayerCard();
        attackerCard.setId(attackerCardId);
        attackerCard.setAttackPoints(10);
        attackerCard.setDefensePoints(10);
        attackerCard.setHasAttacked(false);
        attackerState.getFieldCards().add(attackerCard);

        // Zielkarte initialisieren
        PlayerCard targetCard = new PlayerCard();
        targetCard.setId(targetCardId);
        targetCard.setAttackPoints(5);
        targetCard.setDefensePoints(5);
        botPS.getFieldCards().add(targetCard);

        // Spielinitialisierung
        Game game = new Game();
        game.setId(gameId);
        game.setUsers(List.of(attacker));
        game.setCurrentTurn(attacker);

        //Mocking der Repository-Methoden
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userAccountRepository.findById(attackerId)).thenReturn(Optional.of(attacker));
        when(playerStateRepository.findById(botPSId)).thenReturn(Optional.of(botPS));
        when(playerCardRepository.findById(attackerCardId)).thenReturn(Optional.of(attackerCard));
        when(playerCardRepository.findById(targetCardId)).thenReturn(Optional.of(targetCard));

        // Ausführen der Methode
        gameService.attackBotCard(request);

        // Verifizieren der Ergebnisse
        assertTrue(attackerCard.getHasAttacked());
        assertEquals(-1, targetCard.getDefensePoints());
        assertFalse(botPS.getFieldCards().contains(targetCard));
        assertEquals(6, attackerState.getDamage()); // Damage = targetCard defensePoints + 1 (5 + 1)

        //Verifizieren der Aufrufe der Mock-Methoden
        verify(playerCardRepository).save(attackerCard);
        verify(playerCardRepository).save(targetCard);
        verify(playerStateRepository).save(attackerState);
        verify(playerStateRepository).save(botPS);
        verify(gameRepository).save(game);
    }



    @Test
    void testAttackBot() {
        //IDs für Testinstanzen initialisieren
        Long gameId = 1L;
        Long attackerId = 1L;
        Long botPSId = 2L;
        Long attackerCardId = 1L;

        //Request-Objekt initialisieren
        AttackBotRequest request = new AttackBotRequest();
        request.setGameId(gameId);
        request.setAttackerId(attackerId);
        request.setBotPSId(botPSId);
        request.setAttackerCardId(attackerCardId);

        //UserAccount und zugehörigen PlayerState initialisieren
        UserAccount attacker = new UserAccount();
        attacker.setId(attackerId);
        PlayerState attackerState = new PlayerState();
        attacker.setPlayerState(attackerState);

        //Bot PlayerState initialisieren
        PlayerState botPS = new PlayerState();
        botPS.setId(botPSId);
        botPS.setLifePoints(15);

        //Angreifende Karte initialisieren
        PlayerCard attackerCard = new PlayerCard();
        attackerCard.setId(attackerCardId);
        attackerCard.setAttackPoints(10);
        attackerCard.setHasAttacked(false);
        attackerState.getFieldCards().add(attackerCard);

        //Spielinitialisierung
        Game game = new Game();
        game.setId(gameId);
        game.setFirstRound(false);
        game.setCurrentTurn(attacker);
        game.setUsers(List.of(attacker));
        game.setPlayerStateBot(botPS);

        //Mocking der Repository-Methoden
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userAccountRepository.findById(attackerId)).thenReturn(Optional.of(attacker));
        when(playerStateRepository.findById(botPSId)).thenReturn(Optional.of(botPS));
        when(playerCardRepository.findById(attackerCardId)).thenReturn(Optional.of(attackerCard));

        //Ausführen der Methode
        gameService.attackBot(request);

        //Verifizieren der Ergebnisse
        assertTrue(attackerCard.getHasAttacked());
        assertEquals(5, botPS.getLifePoints()); // 15 - 10 = 5
        assertEquals(10, attackerState.getDamage()); // 10 Angriffspunkte hinzugefügt

        //Verifizieren der Aufrufe der Mock-Methoden
        verify(playerCardRepository).save(attackerCard);
        verify(playerStateRepository).save(attackerState);
        verify(playerStateRepository).save(botPS);
        verify(gameRepository).save(game);
    }



    @Test
    void testTerminateMatchWithBot() {
        //IDs für Testinstanzen initialisieren
        Long gameId = 1L;
        Long userAId = 1L;
        Long botPSId = 2L;

        // Änderung: Mocking der Game-Entity
        Game game = new Game();
        game.setId(gameId);


        //Mocking der UserAccounts
        UserAccount userA = new UserAccount();
        userA.setId(userAId);
        userA.setLeaderboardPoints(100);
        game.setUsers(List.of(userA));

        //Mocking der PlayerState für userA
        PlayerState playerStateA = new PlayerState();
        playerStateA.setWinner(true); // Annahme: userA ist der Gewinner
        playerStateA.setDamage(10); // Beispielwert
        List<PlayerCard> cardsPlayedByA = Arrays.asList(
                createPlayerCard(Rarity.NORMAL, false),
                createPlayerCard(Rarity.NORMAL, false),
                createPlayerCard(Rarity.RARE, false)
        );
        playerStateA.setCardsPlayed(cardsPlayedByA);
        userA.setPlayerState(playerStateA);

        //Mocking der PlayerState für den Bot (game.getPlayerStateBot())
        PlayerState botPlayerState = new PlayerState();
        botPlayerState.setId(botPSId);
        botPlayerState.setDamage(5); // Beispielwert
        List<PlayerCard> cardsPlayedByBot = Arrays.asList(
                createPlayerCard(Rarity.NORMAL, false),
                createPlayerCard(Rarity.LEGENDARY, true),
                createPlayerCard(Rarity.LEGENDARY, false)
        );
        botPlayerState.setCardsPlayed(cardsPlayedByBot);


        //Setzen des game.getPlayerStateBot auf den Bot-PlayerState
        game.setPlayerStateBot(botPlayerState);

        //Mocking der Optional-Rückgaben für die Repository-Aufrufe
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userAccountRepository.findById(userAId)).thenReturn(Optional.of(userA));
        when(playerStateRepository.findById(botPSId)).thenReturn(Optional.of(botPlayerState));

        //Ausführen der Methode
        gameService.terminateMatch(gameId, botPSId , userAId);

        //Verifizieren der Methodenaufrufe
        verify(playerStateRepository).save(playerStateA);
        verify(playerStateRepository).save(botPlayerState);
        verify(userAccountRepository).save(userA);

        //Verifizieren der Spiel- und Nachrichtenlogik
        verify(gameRepository).save(game);
        verify(messagingTemplate).convertAndSendToUser(anyString(), anyString(), anyList());
        verify(messagingTemplate).convertAndSend(anyString(), Optional.ofNullable(any()));


        assertEquals(100, userA.getLeaderboardPoints());
    }

    // Hilfsmethode zur Erstellung einer PlayerCard mit gegebener Rarität und Opfer-Status
    private PlayerCard createPlayerCard(Rarity rarity, boolean sacrificed) {
        PlayerCard card = new PlayerCard();
        card.setRarity(rarity);
        card.setSacrificed(sacrificed);
        return card;
    }


    @Test
    void testWatchStream() {

        Long gameId = 1L;
        Long viewerId = 1L;
        Long user1Id = 2L;
        Long user2Id = 3L;

        // Mocking von Request-Objekt
        WatchStreamRequest request = new WatchStreamRequest();
        request.setGameId(gameId);
        request.setUserId(viewerId);

        UserAccount user1 = new UserAccount();
        user1.setId(user1Id);
        UserAccount user2 = new UserAccount();
        user2.setId(user2Id);

        // Mocking von Game und UserAccount
        Game game = new Game();
        game.setId(gameId);
        game.setUsers(List.of(user1, user2));

        UserAccount viewer = new UserAccount();
        viewer.setId(viewerId);

        // Mocking der Optional-Rückgaben für Repository-Aufrufe
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userAccountRepository.findById(viewerId)).thenReturn(Optional.of(viewer));


        // Ausführen der Methode
        gameService.watchStream(request);

        assertEquals(viewerId, game.getViewers().get(0).getId());
        assertEquals(gameId, viewer.getWatching().getId());
    }

    @Test
    void testLeaveStreamWhenWatchingGame() {

        Long gameId = 1L;
        Long userId = 1L;
        // Mocking von Request-Objekt
        LeaveStreamRequest request = new LeaveStreamRequest();
        request.setUserId(userId);

        // Mocking von UserAccount
        UserAccount user = new UserAccount();
        user.setId(userId);
        Game game = new Game();
        game.setId(gameId);
        user.setWatching(game);

        // Mocking der Optional-Rückgabe für Repository-Aufrufe
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // Ausführen der Methode
        gameService.leaveStream(request);

        assertNull(user.getWatching());
        assertEquals(0, game.getViewers().size());
    }



    @Test
    void testCreateBotGame() {
        // Initialisierung der IDs für Testinstanzen
        Long userId = 1L;
        Long deckId = 1L;

        // Erstellen von Karten für das Deck
        Card card1 = new Card("Card 1", 10, 5, "Description 1", new byte[0], Rarity.NORMAL);
        Card card2 = new Card("Card 2", 8, 3, "Description 2", new byte[0], Rarity.RARE);

        List<Card> cards = new ArrayList<>();
        cards.add(card1);
        cards.add(card2);

        // Initialisierung des Request-Objekts
        CreateBotRequest request = new CreateBotRequest();
        request.setUserId(userId);
        request.setDeckId(deckId);
        request.setStreamed(true); // Simuliere gestreamtes Spiel

        // Mocking der UserAccount und Deck Objekte
        UserAccount user = new UserAccount();
        user.setId(userId);
        user.setUsername("TestUser");

        Deck deck = new Deck();
        deck.setId(deckId);
        deck.setUser(user);
        deck.setCards(cards);

        // Mocking für die Repository-Aufrufe
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
        when(gameRepository.existsByUsersContaining(user)).thenReturn(false); // User ist nicht in einem Spiel

        // Aufruf der zu testenden Methode
        gameService.createBotGame(request);

        // Assertions

        // Verifizieren, dass der Benutzer einen Spielerstatus hat und dieser gespeichert wurde
        assertNotNull(user.getPlayerState());
        assertNotNull(user.getPlayerState().getDeckClone());
        assertEquals(0, user.getPlayerState().getDeckClone().size());

        // Verifizieren, dass der Spielerstatus des Benutzers und des Bots initialisiert wurden
        assertNotNull(user.getPlayerState().getHandCards());
        assertFalse(user.getPlayerState().getHandCards().isEmpty());
        assertNotNull(user.getPlayerState().getFieldCards());
        assertTrue(user.getPlayerState().getFieldCards().isEmpty());

        // Verifizieren, dass das Deck des Bots initialisiert wurde
        assertNotNull(user.getPlayerState().getDeckClone());

    }



}

