package com.example.demo.cards;
import com.example.demo.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class CardInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name= "card_id")
    @JsonIgnore
    private Card card;

    @ManyToOne
    @JoinColumn(name= "user_id")
    @JsonIgnore
    private UserAccount userAccount;

}
