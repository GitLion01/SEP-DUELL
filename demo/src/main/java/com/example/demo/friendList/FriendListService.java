package com.example.demo.friendList;

import com.example.demo.user.UserAccount;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendListService {
    final FriendListRepository friendListRepository;
    public FriendListService(FriendListRepository friendListRepository) {
        this.friendListRepository = friendListRepository;
    }

    public Optional<List<UserAccount>> getFriendList(int id) {
        Optional<UserAccount> user=friendListRepository.findById(id);
        if(user.isPresent())
        {
            UserAccount userAccount=user.get();
            return Optional.of(userAccount.getFriends());
        }
        else
            return Optional.empty();
    }

    public String FriendshipRequest(int id,int friend_id) {
        Optional<UserAccount> user=friendListRepository.findById(id);
        Optional<UserAccount> friend=friendListRepository.findById(friend_id);
        if(user.isPresent() && friend.isPresent())
        {
            UserAccount userAccount=user.get();
            UserAccount friendAccount=friend.get();
            //Request must be saved in the friend account so the friend can see it and accept/decline it
            friendAccount.addFriendRequest(userAccount);
            return "Friend request sent to "+userAccount.getUsername()+" to "+friendAccount.getUsername();
        }
        else
            return "User not found";
    }
}
