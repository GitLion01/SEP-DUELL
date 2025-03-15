package com.example.demo.turnier;

import com.example.demo.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor  // von JPA benötigt, um Entitätsobjekte beim Laden aus der Datenbank zu erstellen
public class Bet {

    @Id // Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bettor_id")
    @JsonIgnore
    private UserAccount bettor;

    @ManyToOne
    @JoinColumn(name = "bet_on_id")
    @JsonIgnore
    private UserAccount betOn;


    private boolean isWinner;   // Gibt an, ob die Wette gewonnen wurde.
    private boolean completed;  // Gibt an, ob die Wette abgeschlossen ist.

    public Bet(UserAccount bettor, UserAccount betOn) {
        this.bettor = bettor;
        this.betOn = betOn;
        this.isWinner = false;
        this.completed = false;
    }
}
