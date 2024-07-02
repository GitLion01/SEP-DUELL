package com.example.demo.turnier;


import com.example.demo.user.UserAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Runde {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String rundeName;

    @OneToMany(cascade=CascadeType.ALL)
    @JoinColumn(name = "round_match")
    private List<Match> match= new ArrayList<>();

    @OneToMany
    @JoinColumn(name= "Winners")
    private List<UserAccount> gewinners=new ArrayList<>();
}
