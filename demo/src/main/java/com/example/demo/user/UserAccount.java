package com.example.demo.user;
import com.example.demo.cards.Card;
import com.example.demo.decks.Deck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private Boolean locked = false;
    private Boolean enabled = false;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deck> decks = new ArrayList<>();

    @ManyToMany
    /*to ignore the infinite loop occurring in the serialization ,when join the two tables
    /if a user accepted the friend request*/
    @JsonIgnore
    @JoinTable(
            name = "friendship",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id"))
    private List<UserAccount> friends=new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "friend_requests",
            // specify the foreign key columns in the join table.
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_requests"))
    private List<UserAccount> friendRequests=new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_cards", // Name of the join table
            joinColumns = @JoinColumn(name = "user_id"), // Column name in the join table for UserAccount
            inverseJoinColumns = @JoinColumn(name = "card_id") // Column name in the join table for Card
    )
    private List<Card> cards = new ArrayList<>();


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

    public void removeCard(Card card) {
        cards.remove(card);
        card.getUsers().remove(this); // Remove the user from the card's collection
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
}
