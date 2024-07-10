package com.example.demo.turnier;

import com.example.demo.user.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bettor_id")
    private UserAccount bettor;

    @ManyToOne
    @JoinColumn(name = "bet_on_id")
    private UserAccount betOn;

    private boolean isWinner;

    private boolean completed; // Neues Feld

    public Bet(UserAccount bettor, UserAccount betOn) {
        this.bettor = bettor;
        this.betOn = betOn;
        this.isWinner = false;
        this.completed = false; // Standardmäßig nicht abgeschlossen
    }
}
