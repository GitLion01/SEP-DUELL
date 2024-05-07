package com.example.demo.freindList;

import com.example.demo.user.UserAccount;

import java.util.List;
import java.util.Optional;

public class FriendlListService {
    private final FriendListRepository friendListRepository;

    public FriendlListService(FriendListRepository friendListRepository) {
        this.friendListRepository = friendListRepository;
    }
    public List<UserAccount> getFriendList(Integer UserId)
    {
        Optional<FriendListRequest> user=friendListRepository.findById(UserId);
        if(user.isPresent())
        {
            user
        }
        else
            throw new IllegalArgumentException ("User not found");
    }
}
