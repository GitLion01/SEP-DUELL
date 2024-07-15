package com.example.demo.clan;


import com.example.demo.chat.GroupRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ClanServiceTest {


    @Mock
    private ClanRepository clanRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private GroupRepository groupRepository;

    private AutoCloseable autoCloseable;
    private ClanService underTest;

    @BeforeEach
    void setUp() {
        autoCloseable= MockitoAnnotations.openMocks(this); //initialize alle Mocks in this class
        underTest = new ClanService(clanRepository,userAccountRepository,groupRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }


    @Test
    void createClan() {
        Clan clan =new Clan();
        clan.setName("clan1");
        clan.setId(1L);
        when(clanRepository.findByName("clan1")).thenReturn(Optional.of(clan));
        Optional<Clan> clanTest= clanRepository.findByName("clan1");

        assertTrue(clanTest.isPresent());
        assertEquals(clanTest.get().getId(),1L);
    }

    @Test
    void getClans() {
        Clan clan =new Clan();
        clan.setName("clan1");
        clan.setId(1L);
        Clan clan2 =new Clan();
        clan2.setName("clan2");
        clan2.setId(2L);
        Clan clan3 =new Clan();
        clan3.setName("clan3");
        clan3.setId(3L);

        when(clanRepository.findByName("clan1")).thenReturn(Optional.of(clan));
        when(clanRepository.findByName("clan2")).thenReturn(Optional.of(clan2));
        when(clanRepository.findByName("clan3")).thenReturn(Optional.of(clan3));

        List<Clan> clans = new ArrayList<>();
        clans.add(clanRepository.findByName("clan1").get());
        clans.add(clanRepository.findByName("clan2").get());
        clans.add(clanRepository.findByName("clan3").get());

        assertEquals(3,clans.size());
        assertEquals(1L,clans.get(0).getId());
        assertEquals(2L,clans.get(1).getId());
        assertEquals(3L,clans.get(2).getId());
    }

    @Test
    void getClanId() {
        Clan clan =new Clan();
        clan.setName("clan1");
        clan.setId(1L);
        when(clanRepository.findByName("clan1")).thenReturn(Optional.of(clan));
        Optional<Clan> clanTest= clanRepository.findByName("clan1");

        assertTrue(clanTest.isPresent());
        assertEquals(clanTest.get().getId(),1L);
    }

    @Test
    void getClanMitglieder() {
        Clan clan =new Clan();
        clan.setName("clan1");
        clan.setId(1L);

        List<UserAccount> userAccounts = new ArrayList<>();
        for(int i=0;i<9;i++){
            UserAccount userAccount = new UserAccount();
            userAccount.setId(i+1L);
            userAccount.setUsername("username"+i);
            userAccount.setFirstName("first"+i);
            userAccount.setLastName("last"+i);
            userAccounts.add(userAccount);
        }
        clan.setUsers(userAccounts);
        when(clanRepository.findByName("clan1")).thenReturn(Optional.of(clan));
        Optional<Clan> clanTest= clanRepository.findByName("clan1");

        assertTrue(clanTest.isPresent());
        for(int i=0;i<9;i++) {
            assertEquals(i + 1L, clan.getUsers().get(i).getId());
            assertEquals("username" + i, clan.getUsers().get(i).getUsername());
            assertEquals("first"+i,clan.getUsers().get(i).getFirstName());
            assertEquals("last"+i,clan.getUsers().get(i).getLastName());
        }

    }

    @Test
    void joinClan() {
        Clan clan =new Clan();
        clan.setName("clan1");
        clan.setId(1L);
        UserAccount user = new UserAccount();
        clan.setUsers(Collections.singletonList(user));

        when(clanRepository.findByName("clan1")).thenReturn(Optional.of(clan));

        Optional<Clan> clanTest= clanRepository.findByName("clan1");
        assertTrue(clanTest.isPresent());
        assertEquals(user,clanTest.get().getUsers().get(0));

    }

    @Test
    void leaveClan() {
        Clan clan =new Clan();
        clan.setName("clan1");
        clan.setId(1L);
        UserAccount user = new UserAccount();
        clan.setUsers(new ArrayList<>(Collections.singletonList(user)));
        clan.getUsers().remove(user);

        when(clanRepository.findByName("clan1")).thenReturn(Optional.of(clan));

        Optional<Clan> clanTest= clanRepository.findByName("clan1");
        assertTrue(clanTest.isPresent());
        assertTrue(clanTest.get().getUsers().isEmpty());
    }
}