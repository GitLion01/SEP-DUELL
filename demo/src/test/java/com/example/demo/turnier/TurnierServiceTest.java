package com.example.demo.turnier;

import com.example.demo.clan.Clan;
import com.example.demo.clan.ClanRepository;
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
import static org.mockito.Mockito.*;

class TurnierServiceTest {

    @Mock
    private TurnierRepository turnierRepository;
    @Mock
    private ClanRepository clanRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private RundeRepository rundeRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private BetRepository betRepository;

    private AutoCloseable autoCloseable;
    TurnierService underTest;

    @BeforeEach
    void setUp() {
        autoCloseable= MockitoAnnotations.openMocks(this); //initialize alle Mocks in this class
        underTest = new TurnierService(turnierRepository, clanRepository,messagingTemplate, userAccountRepository, rundeRepository, matchRepository, betRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void turnierStart() {
        Clan clan = new Clan();
        clan.setName("clan1");
        UserAccount userAccount = new UserAccount();
        userAccount.setClan(clan);
        clan.setTurnier(new Turnier());

        when(clanRepository.findByName("clan1")).thenReturn(Optional.of(clan));
        Optional<Clan> clanTest= clanRepository.findByName("clan1");

        assertTrue(clanTest.isPresent());
        assertEquals(clan.getName(), clanTest.get().getName());
        assertEquals(clan.getTurnier(), clanTest.get().getTurnier());
    }

    @Test
    void turnierAkzeptieren() {
        Clan clan = new Clan();
        clan.setName("clan1");
        UserAccount userAccount = new UserAccount();
        userAccount.setClan(clan);
        Turnier turnier = new Turnier();
        turnier.setId(1L);
        turnier.setAkzeptierteUsers(Collections.singletonList(userAccount));
        clan.setTurnier(turnier);

        when(turnierRepository.findById(1L)).thenReturn(Optional.of(turnier));
        Optional<Turnier> turnierTest= turnierRepository.findById(1L);

        assertTrue(turnierTest.isPresent());
        assertEquals(1,turnier.getAkzeptierteUsers().size());
        assertEquals(userAccount,turnier.getAkzeptierteUsers().get(0));

    }

    @Test
    void turnierAblehnen() {
        // Create and set up the necessary objects
        Clan clan = new Clan();
        clan.setName("clan1");
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1L);
        userAccount.setClan(clan);
        Turnier turnier = new Turnier();
        turnier.setId(1L);
        clan.setTurnier(turnier);

        // Mock the repository behaviors
        when(turnierRepository.findById(1L)).thenReturn(Optional.of(turnier));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(userAccount));

        // Simulate fetching the tournament
        Optional<Turnier> turnierTest = turnierRepository.findById(1L);

        // Ensure the tournament exists initially
        assertTrue(turnierTest.isPresent());
        assertEquals(0, turnier.getAkzeptierteUsers().size());
        assertEquals(new ArrayList<>(), turnier.getAkzeptierteUsers());

        // Simulate the action of rejecting the tournament
        underTest.turnierAblehnen(1L);

        // Verify that the delete method is called on the repository with the correct tournament
        verify(turnierRepository, times(1)).delete(turnier);
    }

    @Test
    void turnierIsReady() {
        Clan clan = new Clan();
        clan.setName("clan1");

        UserAccount userAccount = new UserAccount();
        userAccount.setId(1L);
        UserAccount userAccount2 = new UserAccount();
        userAccount2.setId(2L);

        clan.setUsers(Collections.singletonList(userAccount));
        clan.setUsers(Collections.singletonList(userAccount2));

        Turnier turnier = new Turnier();
        turnier.setId(1L);
        turnier.setAkzeptierteUsers(Collections.singletonList(userAccount));
        turnier.setAkzeptierteUsers(Collections.singletonList(userAccount2));

        when(turnierRepository.findById(1L)).thenReturn(Optional.of(turnier));
        when(clanRepository.findById(1L)).thenReturn(Optional.of(clan));

        assertEquals(clan.getUsers().size(),turnier.getAkzeptierteUsers().size());
    }

    @Test
    void getTurnierMatches() {
        // Set up the Clan and related objects
        Clan clan = new Clan();
        clan.setId(1L);
        clan.setName("clan1");

        Turnier turnier = new Turnier();
        turnier.setId(1L);

        Runde runde = new Runde();
        runde.setRundeName("Final Round");

        Match match1 = new Match();
        match1.setId(1L);

        Match match2 = new Match();
        match2.setId(2L);

        List<Match> matches = Arrays.asList(match1, match2);
        runde.setMatch(matches);

        List<Runde> runden = Collections.singletonList(runde);
        turnier.setRunde(runden);

        clan.setTurnier(turnier);

        // Mock the repository behavior
        when(clanRepository.findById(1L)).thenReturn(Optional.of(clan));

        // Call the method
        ResponseEntity<List<Match>> response = underTest.getTurnierMatches(1L);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(matches, response.getBody());
    }

    @Test
    void gewinnerSpeichern_UserIsSoleWinner() {
        // Arrange
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setSepCoins(100);

        Clan clan = new Clan();
        clan.setId(1L);

        Turnier turnier = new Turnier();
        turnier.setId(1L);
        turnier.setClan(clan);

        Runde runde = new Runde();
        runde.setId(1L);

        runde.setGewinners(new ArrayList<>());
        runde.setMatch(new ArrayList<>(Collections.singletonList(new Match())));

        turnier.setRunde(new ArrayList<>(Collections.singletonList(runde)));
        clan.setTurnier(turnier);
        user.setClan(clan);

        when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));

        // Act
        underTest.GewinnerSpeichern(user);

        // Assert
        assertEquals(800, user.getSepCoins());
    }

    @Test
    void checkAccepted() {
        // Arrange
        Turnier turnier = new Turnier();
        turnier.setId(1L);

        UserAccount user = new UserAccount();
        user.setId(1L);

        turnier.setAkzeptierteUsers(Collections.singletonList(user));

        when(turnierRepository.findById(1L)).thenReturn(Optional.of(turnier));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        boolean isAccepted = underTest.checkAccepted(1L, 1L);

        // Assert
        assertTrue(isAccepted);
        verify(turnierRepository, times(1)).findById(1L);
        verify(userAccountRepository, times(1)).findById(1L);
    }

    @Test
    void getGewinner() {
        UserAccount winner1 = new UserAccount();
        winner1.setId(1L);

        UserAccount winner2 = new UserAccount();
        winner2.setId(2L);

        Runde runde = new Runde();
        runde.setGewinners(Arrays.asList(winner1, winner2));

        Turnier turnier = new Turnier();
        turnier.setRunde(Collections.singletonList(runde));

        Clan clan = new Clan();
        clan.setId(1L);
        clan.setTurnier(turnier);

        when(clanRepository.findById(1L)).thenReturn(Optional.of(clan));

        // Act
        List<Long> gewinnerIds = underTest.getGewinner(1L);

        // Assert
        assertEquals(Arrays.asList(1L, 2L), gewinnerIds);
        verify(clanRepository, times(1)).findById(1L);
    }
}