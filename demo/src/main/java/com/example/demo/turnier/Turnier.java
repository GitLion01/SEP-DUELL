package com.example.demo.turnier;


import com.example.demo.clan.Clan;
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
public class Turnier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade=CascadeType.ALL)
    @JoinColumn(name= "runde")
    private List<Runde> runde=new ArrayList<>();

    @OneToOne(mappedBy = "turnier")
    //@JoinColumn(name= "clan_turnier")
    private Clan clan;

    @OneToMany
    @JoinColumn(name = "TurnierAccepted")
    private List<UserAccount> akzeptierteUsers=new ArrayList<>();

}
