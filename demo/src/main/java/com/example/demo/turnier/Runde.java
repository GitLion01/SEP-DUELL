package com.example.demo.turnier;


import com.example.demo.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private List<Match> match= new ArrayList<>();

    @OneToMany
    @JoinColumn(name= "Winners")
    @JsonIgnore
    private List<UserAccount> gewinners=new ArrayList<>();
}
