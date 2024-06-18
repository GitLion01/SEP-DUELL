package com.example.demo.duellHerausforderung;


import com.example.demo.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Transactional
public class HerausforderungService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserAccountRepository userAccountRepository;

    public void sendHerausforderung(Long senderId, Long receiverId) {
        Notification notification = new Notification(senderId,receiverId,userAccountRepository.findById(senderId).get().getUsername(),"challenge");
        messagingTemplate.convertAndSendToUser(receiverId.toString(),"/queue/notifications",notification);
    }

    public void acceptHerausforderung(Long senderId, Long receiverId) {
        Notification notification = new Notification(senderId,receiverId,userAccountRepository.findById(senderId).get().getUsername(),"duelAccepted");
        messagingTemplate.convertAndSendToUser(senderId.toString(),"/queue/notifications",notification);
        messagingTemplate.convertAndSendToUser(receiverId.toString(),"/queue/notifications",notification);
    }

    public void rejectHerausforderung(Long senderId, Long receiverId) {
        Notification notification = new Notification(senderId,receiverId,userAccountRepository.findById(senderId).get().getUsername(),"duelRejected");
        messagingTemplate.convertAndSendToUser(senderId.toString(),"/queue/notifications",notification);
        messagingTemplate.convertAndSendToUser(receiverId.toString(),"/queue/notifications",notification);
    }
}
