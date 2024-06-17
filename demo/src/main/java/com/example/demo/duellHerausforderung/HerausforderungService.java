package com.example.demo.duellHerausforderung;


import com.example.demo.game.Game;
import com.example.demo.game.GameRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class HerausforderungService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserAccountRepository userAccountRepository;
    private final GameRepository gameRepository;

    public void sendHerausforderung(Long senderId, Long receiverId) {
        Notification notification = new Notification(senderId,receiverId,userAccountRepository.findById(senderId).get().getUsername(),"challenge");
        messagingTemplate.convertAndSendToUser(receiverId.toString(),"/queue/notifications",notification);
    }

    public void acceptHerausforderung(Long senderId, Long receiverId) {
        Notification notification = new Notification(senderId,receiverId,userAccountRepository.findById(senderId).get().getUsername(),"duelAccepted");
        messagingTemplate.convertAndSendToUser(receiverId.toString(),"/queue/notifications",notification);
    }

    public ResponseEntity<Long> toDuell(Long senderId, Long receiverId) {
        UserAccount user1 = userAccountRepository.findById(senderId).get();
        UserAccount user2 = userAccountRepository.findById(receiverId).get();

        Game game = new Game();
        game.getUsers().add(user1);
        game.getUsers().add(user2);
        return new ResponseEntity<>(gameRepository.findById(gameRepository.save(game).getId()).get().getId(), HttpStatus.OK);
    }
}
