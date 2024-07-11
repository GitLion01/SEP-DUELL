package com.example.demo.user;
import com.example.demo.cards.CardInstance;
import com.example.demo.chat.Chat;
import com.example.demo.chat.ChatMessage;
import com.example.demo.clan.Clan;
import com.example.demo.decks.Deck;
import com.example.demo.game.Game;
import com.example.demo.game.PlayerState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.turnier.Bet;

import java.util.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class UserAccount implements UserDetails {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private Date dateOfBirth;
    private Integer leaderboardPoints = 0;
    private Integer sepCoins = 500;
    private byte[] image;

    private String status = "offline"; // Standardstatus ist offline -> online, im Duell, offline
    private String duelStatus = "available"; // Neuer Status für Duell: available, in_duel, challenged

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Game watching;

    @ManyToOne                              //für Duell Herausforderung von
    @JoinColumn(name = "challenger_id")
    @JsonIgnore
    private UserAccount challenger; // Der Benutzer, der diesen Benutzer herausgefordert hat

    @Enumerated(EnumType.STRING)
    private UserRole role;
    private Boolean locked = false;
    private Boolean enabled = false;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Deck> decks = new ArrayList<>();
    private Boolean privateFriendList =false;

    @ManyToMany(fetch = FetchType.LAZY)
    /*to ignore the infinite loop occurring in the serialization ,when join the two tables
    /if a user accepted the friend request*/
    @JsonIgnore
    @JoinTable(
            name = "friendship",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id"))
    private List<UserAccount> friends=new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(
            name = "friend_requests",
            // specify the foreign key columns in the join table.
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_requests"))
    private List<UserAccount> friendRequests=new ArrayList<>();

    // mappedBy : um das besitzende Seite der Verbindung zu definieren
    // es gibt immer eine besitzende Seite bei bidirektionalen Beziehung zwischen zwei Entitäten
    @OneToMany(mappedBy = "userAccount", fetch = FetchType.LAZY)
    private List<CardInstance> userCardInstance=new ArrayList<>();

    //Turnierwetten
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Bet> bets = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER,  cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "player_state_id")
    private PlayerState playerState;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name= "user_message")
    private List<ChatMessage> userMessage=new ArrayList<>();


    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JsonIgnore
    @JoinTable(
            name = "userChat",
            joinColumns = @JoinColumn(name = "userAccount_id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id")
    )
    private List<Chat> userChat=new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_clan")
    @JsonIgnore
    private Clan clan;

    private boolean isInTurnier=false;

    public UserAccount(byte[] image,
                       String firstName,
                       String lastName,
                       Date dateOfBirth,
                       String username,
                       String email,
                       String password,
                       UserRole role) {
        this.image = image;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;


    }

    public UserAccount(
                       String firstName,
                       String lastName,
                       Date dateOfBirth,
                       String username,
                       String email,
                       String password,
                       UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;

    }

    public void addFriend(UserAccount friend) {
        friends.add(friend);
    }

    public void removeFriend(UserAccount friend) {
        friends.remove(friend);
    }

    public void addFriendRequest(UserAccount requester) {
        friendRequests.add(requester);
    }

    public void removeFriendRequest(UserAccount requester) {
        friendRequests.remove(requester);
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


    // Neue Methoden für Duell Herausforderung

    public boolean canChallenge(UserAccount other) {
        return this != other && this.isOnline() && other.isOnline() && !this.isInDuel() && !other.isInDuel() && this.hasDeck() && other.hasDeck();
    }

    public boolean isOnline() {
        return "online".equals(this.status);
    }

    public boolean isInDuel() {
        return "in_duel".equals(this.duelStatus);
    }

    public boolean hasDeck() {
        return !this.decks.isEmpty();
    }
}
