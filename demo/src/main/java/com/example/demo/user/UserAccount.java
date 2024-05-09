package com.example.demo.user;
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
    private Integer id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private Date dateOfBirth;
    private Integer leaderboardPoints = 0;
    private Integer sepCoins = 500;
    @Lob
    private byte[] image;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private Boolean locked = false;
    private Boolean enabled = false;

    @Getter
    @ManyToMany
    @JoinTable(
            name = "friendship",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id"))
    private List<UserAccount> friends=new ArrayList<>();

    @Getter
    @ManyToMany
    @JoinTable(
            name = "friend_requests",
            // specify the foreign key columns in the join table.
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_requests"))
    private List<UserAccount> friendRequests=new ArrayList<>();

    public UserAccount(String firstName,
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
}
