package com.example.demo.game;


import com.example.demo.user.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<UserAccount> players;

    // 0 oder 1
    private Integer currentTurn = 0;

    public Boolean isUsersTurn(UserAccount user){
        List<UserAccount> players = this.players;
        Integer currentTurn = this.currentTurn;
        return players.get(currentTurn).equals(user);
    }


}
