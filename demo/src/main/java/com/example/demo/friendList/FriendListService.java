package com.example.demo.friendList;

import com.example.demo.email.EmailSender;
import com.example.demo.user.UserAccount;
import com.example.demo.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.demo.user.UserRole.ADMIN;

@Service
@Transactional
public class FriendListService {
    final FriendListRepository friendListRepository;
    private final EmailSender emailSender;

    public FriendListService(FriendListRepository friendListRepository, EmailSender emailSender) {
        this.friendListRepository = friendListRepository;
        this.emailSender = emailSender;
    }

    // Konvertiert UserAccount zu UserDTO
    private UserDTO convertToDTO(UserAccount user) {
        byte[] image = user.getImage();
        String imageBase64 = null;
        if(image != null) {
            imageBase64 = Base64.getEncoder().encodeToString(user.getImage());
        }
        return new UserDTO(user.getUsername(), imageBase64,user.getId(),user.getFirstName(),user.getLastName(),user.getFriends(), user.getLeaderboardPoints());
    }

    // Gibt die Freundesliste als DTOs zurück
    public Optional<List<UserDTO>> getFriendList(long id) {
        Optional<UserAccount> user = friendListRepository.findById(id);
        return user.map(u -> u.getFriends().stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    public Optional<List<UserDTO>> getFriendsFriendList(long id,long friendID) {
        Optional<UserAccount> user = friendListRepository.findById(id);
        Optional<UserAccount> friend = friendListRepository.findById(friendID);
        if(user.isPresent() && friend.isPresent()) {
            if(user.get().getRole()==ADMIN)
                return friend.map(u -> u.getFriends().stream().map(this::convertToDTO).collect(Collectors.toList()));
            if(friend.get().getPrivateFriendList())
                return Optional.empty();
            if(user.get().getFriends().contains(friend.get()))
                return friend.map(u -> u.getFriends().stream().map(this::convertToDTO).collect(Collectors.toList()));
        }
        return Optional.empty();
    }
/*
    // Gibt die eingehenden Freundschaftsanfragen als DTOs zurück
    public Optional<List<UserDTO>> getFriendListRequests(int id) {
        Optional<UserAccount> user = friendListRepository.findById(id);
        return user.map(u -> u.getFriendRequests().stream().map(this::convertToDTO).collect(Collectors.toList()));
    }
*/
    // Verarbeitet eine Freundschaftsanfrage
    public String friendshipRequest(long id, long friend_id) {
        Optional<UserAccount> user = friendListRepository.findById(id);
        Optional<UserAccount> friend = friendListRepository.findById(friend_id);
        if (user.isPresent() && friend.isPresent()) {
            UserAccount userAccount = user.get();
            UserAccount friendAccount = friend.get();
            if (friendAccount.getFriendRequests().contains(userAccount))
                return "You have already sent a request";
            if (userAccount.getFriendRequests().contains(friendAccount))
                return "This user has already sent you a request";
            if (friendAccount.getFriends().contains(userAccount))
                return "You are already friends";
            if (id == friend_id)
                return "You cannot send a request to yourself";
            friendAccount.addFriendRequest(userAccount);

            // Send email notification
            String acceptLink = String.format("http://localhost:8080/friendlist/accept?userId=%d&friendId=%d", friend_id, id);
            String rejectLink = String.format("http://localhost:8080/friendlist/reject?userId=%d&friendId=%d", friend_id, id);
            String emailContent = buildFriendRequestEmail(userAccount.getUsername(), acceptLink, rejectLink);
            emailSender.send(friendAccount.getEmail(), emailContent);

            return "Friend request sent from " + userAccount.getUsername() + " to " + friendAccount.getUsername();
        } else {
            return "User not found";
        }
    }

    // E-Mail-Vorlage für Freundschaftsanfragen
    private String buildFriendRequestEmail(String requesterName, String acceptLink, String rejectLink) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "<p>Hey, du hast eine Freundschaftsanfrage von " + requesterName + " bekommen. Möchtest du diese annehmen oder ablehnen?</p>\n" +
                "<a href=\"" + acceptLink + "\">Akzeptieren</a> | <a href=\"" + rejectLink + "\">Ablehnen</a>\n" +
                "</div>";
    }

    // Akzeptiert eine Freundschaftsanfrage
    public String friendshipAccept(long id, long friend_id) {
        Optional<UserAccount> user = friendListRepository.findById(id);
        Optional<UserAccount> friend = friendListRepository.findById(friend_id);
        if (user.isPresent() && friend.isPresent()) {
            UserAccount userAccount = user.get();
            UserAccount friendAccount = friend.get();

            if (userAccount.getFriendRequests().contains(friendAccount)) {
                friendAccount.addFriend(userAccount);
                userAccount.addFriend(friendAccount);
                userAccount.removeFriendRequest(friendAccount);
                return "Friendship accepted";
            }
            return "The request is already deleted/do not exist";
        } else {
            return "User not found";
        }
    }

    // Lehnt eine Freundschaftsanfrage ab
    public String friendshipReject(long id, long friend_id) {
        Optional<UserAccount> user = friendListRepository.findById(id);
        Optional<UserAccount> friend = friendListRepository.findById(friend_id);
        if (user.isPresent() && friend.isPresent()) {
            UserAccount userAccount = user.get();
            UserAccount friendAccount = friend.get();

            if (userAccount.getFriendRequests().contains(friendAccount)) {
                userAccount.removeFriendRequest(friendAccount);
                return "Friendship rejected";
            }
            return "The request is already deleted/do not exist";
        } else {
            return "User not found";
        }
    }

    // Entfernt einen Freund aus der Freundesliste
    public ResponseEntity<String> removeFriend(long id, long friend_id) {
        Optional<UserAccount> user = friendListRepository.findById(id);
        Optional<UserAccount> friend = friendListRepository.findById(friend_id);
        if (user.isPresent() && friend.isPresent()) {
            UserAccount userAccount = user.get();
            UserAccount friendAccount = friend.get();

            if (friendAccount.getFriends().contains(userAccount)) {
                userAccount.removeFriend(friendAccount);
                friendAccount.removeFriend(userAccount);
                return new ResponseEntity<>("{\"message\":\"Friendship removed\"}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"message\":\"You are not friends\"}", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("{\"message\":\"User not found\"}", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Void> setFriendslistPrivacy(long userId)
    {
        Optional<UserAccount> user = friendListRepository.findById(userId);
        if(user.isPresent()) {
            UserAccount userAccount = user.get();
            userAccount.setPrivateFriendList(!userAccount.getPrivateFriendList());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Boolean> getFriendslistPrivacy(long userId)
    {
        Optional<UserAccount> user = friendListRepository.findById(userId);
        if(user.isPresent()) {
            UserAccount userAccount = user.get();
            return new ResponseEntity<>(userAccount.getPrivateFriendList(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
