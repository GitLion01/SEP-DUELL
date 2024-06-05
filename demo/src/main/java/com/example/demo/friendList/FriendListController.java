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
    public String FriendshipRequest(@RequestBody List<Long> request) {
        return friendListService.friendshipRequest(request.get(0), request.get(1));
    }

    @GetMapping(path = "/accept")
    public String FriendshipAccept(@RequestParam("userId") long userId, @RequestParam("friendId") long friendId) {
        return friendListService.friendshipAccept(userId, friendId);
    }

    @GetMapping(path = "/reject")
    public String FriendshipReject(@RequestParam("userId") long userId, @RequestParam("friendId") long friendId) {
        return friendListService.friendshipReject(userId, friendId);
    }

    @PostMapping(path = "/remove")
    public ResponseEntity<String> RemoveFriend(@RequestBody List<Integer> request) {
        return friendListService.removeFriend(request.get(0), request.get(1));
    }


    @PutMapping(path = "/setFriendslistPrivacy")
    public ResponseEntity<Void> setFriendslistPrivacy(@RequestParam("userId") long userId) {
        return friendListService.setFriendslistPrivacy(userId);
    }

    @GetMapping(path = "/getFriendslistPrivacy")
    public ResponseEntity<Boolean> getFriendslistPrivacy(@RequestParam("userId") long userId) {
        return friendListService.getFriendslistPrivacy(userId);
    }
}
