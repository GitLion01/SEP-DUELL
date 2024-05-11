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

    @GetMapping(path ="requests/{id}")
    public List<UserAccount> getFriendListRequests(@PathVariable int id) {
        return friendListService.getFriendListRequests(id).orElse(null);
    }

    @PostMapping(path = "/add")
    public String FriendshipRequest(@RequestBody List<Integer> request) {
        return friendListService.FriendshipRequest(request.get(0),request.get(1));
    }
    @PostMapping(path = "/accept")
    public String FriendshipAccept(@RequestBody List<Integer> request) {
        return friendListService.FriendshipAccept(request.get(0),request.get(1));
    }
    @PostMapping(path = "/reject")
    public String FriendshipReject(@RequestBody List<Integer> request) {
        return friendListService.FriendshipReject(request.get(0),request.get(1));
    }
    @PostMapping(path = "/remove")
    public String RemoveFriend(@RequestBody List<Integer> request) {
        return friendListService.RemoveFriend(request.get(0),request.get(1));
    }
}
