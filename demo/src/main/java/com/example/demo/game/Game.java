package com.example.demo.game;
import com.example.demo.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean ready = false;
    private Boolean firstRound = true;
    private Integer timeLeft = 120;
    private Boolean streamed = false;
    private Long botDeckId;

    @OneToMany
    @JoinTable(
            name = "game_users",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<UserAccount> users = new ArrayList<>();

    @OneToMany
    @JsonIgnore
    private List<UserAccount> viewers = new ArrayList<>();

    @OneToOne
    private PlayerState playerStateBot;

    @OneToOne(fetch = FetchType.EAGER)
    private UserAccount currentTurn;

    private Boolean myTurn = true;

    public void resetTimer(){
        this.timeLeft = 120;
    }

    public Integer getRemaingTime(){
        return this.timeLeft;
    }

    public void decrementTimer(){
        if(this.timeLeft > 0){
            timeLeft--;
        }
    }
}
