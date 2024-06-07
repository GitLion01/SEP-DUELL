package com.example.demo.game;


import com.example.demo.user.UserAccount;
import jakarta.persistence.*;


import java.util.List;


@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<UserAccount> players;

    // 0 oder 1
    private Integer currentTurn = 0;


}
