package com.example.demo.user;

import com.example.demo.decks.Deck;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString

public class UserAccountResponse {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Date dateOfBirth;
    private Integer leaderboardPoints = 0;
    private Integer sepCoins = 500;
    private String image;
    private UserRole role;
    private Boolean locked = false;
    private Boolean enabled = false;
    private List<Deck> decks = new ArrayList<>();
}
