package com.example.demo.clan;

import com.example.demo.chat.Group;
import com.example.demo.user.UserAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Clan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name= "clan_id")
    private List<UserAccount> users =new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Group group = new Group(name);
}
