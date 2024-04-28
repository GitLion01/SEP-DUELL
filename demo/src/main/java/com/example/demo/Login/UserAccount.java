package com.example.demo.Login;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public abstract class UserAccount{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Date dateOfBirth;
    private Integer leaderboardPoints = 0;
    private Integer sepCoins = 500;
    @Lob
    private byte[] image;
    private Boolean isAdmin;


    public abstract void viweProfile(Integer userID);
    public abstract void login(String email, String password);





}