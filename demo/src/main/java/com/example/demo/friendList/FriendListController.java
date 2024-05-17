package com.example.demo.friendList;
import com.example.demo.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/friendlist")
@CrossOrigin
@AllArgsConstructor
public class FriendListController {

    private final FriendListService friendListService;

    @GetMapping(path = "/{id}")
    public List<UserDTO> getFriends(@PathVariable int id) {
        return friendListService.getFriendList(id).orElse(null);
    }
    @GetMapping(path ="/{id}/friends/{friendId}")
    public List<UserDTO> getFriendsFriendList(@PathVariable int id,@PathVariable int friendId) {
        return friendListService.getFriendsFriendList(id,friendId).orElse(null);
    }

    @PostMapping(path = "/add")
    public String friendshipRequest(@RequestBody List<Integer> request) {
        return friendListService.FriendshipRequest(request.get(0), request.get(1));
    }

    @GetMapping(path = "/accept")
    public String friendshipAccept(@RequestParam("userId") int userId, @RequestParam("friendId") int friendId) {
        return friendListService.FriendshipAccept(userId, friendId);
    }

    @GetMapping(path = "/reject")
    public String friendshipReject(@RequestParam("userId") int userId, @RequestParam("friendId") int friendId) {
        return friendListService.FriendshipReject(userId, friendId);
    }

    @PostMapping(path = "/remove")
    public ResponseEntity<String> removeFriend(@RequestBody List<Integer> request) {
        return friendListService.RemoveFriend(request.get(0), request.get(1));
    }
}
