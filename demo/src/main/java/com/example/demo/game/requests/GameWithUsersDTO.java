package com.example.demo.game.requests;
import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameWithUsersDTO {

    private Long gameId;
    private List<UserAccount> users;

}

