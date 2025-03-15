package com.example.demo.clan;

import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClanDTO {
    private Long id;
    private String name;
    private final List<UserAccount> userAccount=new ArrayList<>();
}
