package com.example.demo.chat;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class ChatService {

    private final UserAccountRepository userAccountRepository;
    private final ChatRepository chatRepository;
    private ChatMessageRepository chatMessageRepository;
    //private SimpMessagingTemplate messagingTemplate;


    //@org.springframework.transaction.annotation.Transactional
    public void createChat(Long userId1,Long userId2)
    {
        Optional<UserAccount> user1 = userAccountRepository.findById(userId1);
        Optional<UserAccount> user2 = userAccountRepository.findById(userId2);
        if(user1.isPresent() && user2.isPresent())
        {
            UserAccount u1 = user1.get();
            UserAccount u2 = user2.get();

            // Debugging: Print user chats
            System.out.println("User1 Chats: " + u1.getUserChat().size());
            for (Chat chat : u1.getUserChat()) {
                System.out.println("Chat ID: " + chat.getId());
            }

            boolean exists = false;
            System.out.println("Helllllllllllllllllllllllllllllllo");
            for(Chat chat : u1.getUserChat())
            {
                System.out.println(chat.getId()+"-------------------");
                if(chat.getUsers().contains(u2)) {
                    System.out.println("old Chat--------------------------");
                    exists = true;
                    break;
                }
            }
            if(!exists)
            {
                System.out.println("new Chat--------------------------");
                Chat newChat = new Chat();
                newChat.getUsers().add(u1);
                newChat.getUsers().add(u2);
                u1.getUserChat().add(newChat);
                u2.getUserChat().add(newChat);
                chatRepository.save(newChat);
                userAccountRepository.save(u1);
                userAccountRepository.save(u2);
            }
        }
    }

    public void sendMessage(ChatMessage chatMessage)
    {
        chatMessageRepository.save(chatMessage);
    }
}
