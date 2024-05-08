package com.example.demo.friendList;
import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/friendlist")
@CrossOrigin
@AllArgsConstructor
public class FriendListController {

    private final FriendListService friendListService;
    @GetMapping(path ="/{id}")
    public List<UserAccount> getFriends(@PathVariable int id) {
        return friendListService.getFriendList(id).orElse(null);
    }

    @GetMapping
    public String FriendshipRequest(@RequestParam int id, @RequestParam int friend_id) {
        return friendListService.FriendshipRequest(id,friend_id);
    }



}
