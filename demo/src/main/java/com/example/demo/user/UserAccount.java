package com.example.demo.user;
import com.example.demo.cards.Card;
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
    /*
    public void addCard(Card card) {
        cards.add(card);
    }
    public void removeCard(Card card) {
        cards.remove(card);
    }
*/
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
