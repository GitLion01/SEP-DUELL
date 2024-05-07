package com.example.demo.freindList;
import com.example.demo.user.UserAccount;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class FriendListRequest {
    @OneToOne(mappedBy = "friendList")
    private UserAccount userAccount;
    List<Integer> friends;
    List<Integer> waitingRequests;
    boolean visibility;

    public List<Integer> getFriends() {

    }

}
