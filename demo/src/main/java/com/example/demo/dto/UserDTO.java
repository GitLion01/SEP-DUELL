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
    private String clanName;
    private String status;


    public UserDTO(Long id,String username,Integer leaderboardPoints,String status){
        this.id=id;
        this.username = username;
        this.leaderboardPoints = leaderboardPoints;
        this.status = status;
    }
    // Konstruktoren, Getter und Setter
    public UserDTO( String username,long id,String firstName,String lastName) {
        this.username = username;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserDTO(String username, String image, long id, String firstName, String lastName, List<UserAccount> friends,Integer leaderboardPoints, String clanName) {
        this.username = username;
        this.image = image;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.friends = friends;
        this.leaderboardPoints = leaderboardPoints;
        this.clanName = clanName;
    }

    public UserDTO(String username, String image, Long id, String firstName, String lastName, List<UserAccount> friends, Integer leaderboardPoints) {
        this.username = username;
        this.image = image;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.friends = friends;
        this.leaderboardPoints = leaderboardPoints;
    }
}
