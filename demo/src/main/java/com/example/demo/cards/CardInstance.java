package com.example.demo.cards;
import com.example.demo.user.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CardInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name= "card_id")
    private Card card;

    @ManyToOne
    @JoinColumn(name= "user_id")
    private UserAccount userAccount;
}
