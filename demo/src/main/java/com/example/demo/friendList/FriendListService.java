package com.example.demo.friendList;

import com.example.demo.user.UserAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
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

    public Optional<List<UserAccount>> getFriendListRequests(int id) {
        Optional<UserAccount> user=friendListRepository.findById(id);
        if(user.isPresent())
        {
            UserAccount userAccount=user.get();
            return Optional.of(userAccount.getFriendRequests());
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
            if(friendAccount.getFriendRequests().contains(userAccount))
                return "You have already sent a request";
            if(userAccount.getFriendRequests().contains(friendAccount))
                return "This user have already sent you a request";
            if(friendAccount.getFriends().contains(userAccount))
                return "You are already friends";
            if(id==friend_id)
                return "You can not send a request to yourself";
            //Request must be saved in the friend account so the friend can see it and accept/decline it
            friendAccount.addFriendRequest(userAccount);
            return "Friend request sent from "+userAccount.getUsername()+" to "+friendAccount.getUsername();
        }
        else
            return "User not found";
    }

    public String FriendshipAccept(int id,int friend_id) {
        Optional<UserAccount> user=friendListRepository.findById(id);
        Optional<UserAccount> friend=friendListRepository.findById(friend_id);
        if(user.isPresent() && friend.isPresent()) {
            UserAccount userAccount = user.get();
            UserAccount friendAccount = friend.get();

            if(userAccount.getFriendRequests().contains(friendAccount)) {
                friendAccount.addFriend(userAccount);
                userAccount.addFriend(friendAccount);
                userAccount.removeFriendRequest(friendAccount);
                return "Friendship accepted";
            }
            return "the request is already deleted/do not exist";
        }
        else
            return "User not found";
    }

    public String FriendshipReject(int id,int friend_id) {
        Optional<UserAccount> user=friendListRepository.findById(id);
        Optional<UserAccount> friend=friendListRepository.findById(friend_id);
        if(user.isPresent() && friend.isPresent()) {
            UserAccount userAccount=user.get();
            UserAccount friendAccount=friend.get();

            if(userAccount.getFriendRequests().contains(friendAccount)) {
                userAccount.removeFriendRequest(friendAccount);
                return "Friendship rejected";
            }
            return "the request is already deleted/do not exist";
        }
        else
            return "User not found";
    }

    public String RemoveFriend(int id,int friend_id) {
        Optional<UserAccount> user=friendListRepository.findById(id);
        Optional<UserAccount> friend=friendListRepository.findById(friend_id);
        if(user.isPresent() && friend.isPresent()) {
            UserAccount userAccount=user.get();
            UserAccount friendAccount=friend.get();

            if(friendAccount.getFriends().contains(userAccount))
            {
                userAccount.removeFriend(friendAccount);
                friendAccount.removeFriend(userAccount);
                return "Friendship removed";
            }
            else
                return "You are not friends";
        }
        else
            return "User not found";
    }
}
