package com.example.demo.chat;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@CrossOrigin
@RestController
public class ChatController {

    @Autowired
    private UserAccountRepository userAccount;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    @PostMapping("/create.chat")
    public Chat createChat(@RequestParam Long userId1, @RequestParam Long userId2)
    {
        // TODO Use Service and do not create a new chat every time
        Optional<UserAccount> user1 = userAccount.findById(userId1);
        Optional<UserAccount> user2 = userAccount.findById(userId2);
        if(user1.isPresent() && user2.isPresent())
        {
            UserAccount u1 = user1.get();
            UserAccount u2 = user2.get();
            Chat chat = new Chat();
            chat.getUsers().add(u1);
            chat.getUsers().add(u2);

            return chatRepository.save(chat);
        }
        return null;
    }

    @MessageMapping("/sendMessage")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage)
    {
        chatMessageRepository.save(chatMessage);
        chatMessageRepository.save(chatMessage);
        System.out.println("--------------------------------------");
        messagingTemplate.convertAndSend("/topic", chatMessage);
        return chatMessage;
    }

}
