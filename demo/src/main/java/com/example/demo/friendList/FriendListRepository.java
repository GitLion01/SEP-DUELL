package com.example.demo.friendList;

import com.example.demo.user.UserAccount;
import org.springframework.data.repository.CrudRepository;

public interface FriendListRepository extends CrudRepository<UserAccount,Integer> {
}
