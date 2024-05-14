package com.example.demo.dto;

import com.example.demo.user.UserAccount;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO {

    private String username;
    private String image;
    private long id;
    private String firstName;
    private String lastName;
    private List<UserAccount> friends;
    private Integer leaderboardPoints;

    // Konstruktoren, Getter und Setter
    public UserDTO() {
    }

    public UserDTO(String username, String image, long id, String firstName, String lastName, List<UserAccount> friends,Integer leaderboardPoints) {
        this.username = username;
        this.image = image;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.friends = friends;
        this.leaderboardPoints = leaderboardPoints;
    }

}
