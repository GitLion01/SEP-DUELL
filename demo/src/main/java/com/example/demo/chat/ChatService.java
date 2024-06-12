package com.example.demo.chat;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class ChatService {

    private final UserAccountRepository userAccountRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
            for(Chat chat : u1.getUserChat())
            {
                if(chat.getUsers().contains(u2)) {
                    exists = true;
                    break;
                }
            }
            if(!exists)
            {
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
        try {
            for (Chat chat : chatRepository.findAll()) {
                if (Objects.equals(chatMessage.getChat().getId(), chat.getId()))
                {
                    chat.getMessages().add(chatMessage);
                    //we do not need to add it in the chatMessageRepository  it will be added automatically
                    chatRepository.save(chat);
                    chatMessage.setId(chat.getMessages().get(chat.getMessages().size() - 1).getId());

                    Long id= chat.getUsers().get(0).getId();
                    if(chat.getUsers().get(0).getId().equals(chatMessage.getSender().getId()))
                        id = chat.getUsers().get(1).getId();

                    messagingTemplate.convertAndSendToUser(id.toString(),"/queue/messages", convertToDTO(chatMessage));//messagingTemplate.convertAndSend("/topic", convertToDTO(chatMessage));
                    break;
                }
            }

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void editMessage(ChatMessage chatMessage) {
        ChatMessage existingMessage = chatMessageRepository.findById(chatMessage.getId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        existingMessage.setMessage(chatMessage.getMessage());
        chatMessageRepository.save(existingMessage);
        updateChatWithEditedMessage(existingMessage);

        messagingTemplate.convertAndSend("/topic", convertToDTO(existingMessage));
    }

    public void updateChatWithEditedMessage(ChatMessage chatMessage) {
        for(Chat chat : chatRepository.findAll()) {
            if(chatMessage.getChat().getId().equals(chat.getId())) {
                chatRepository.save(chat);
            }
        }
    }

    public void deleteMessage(ChatMessage chatMessage) {
        Optional<ChatMessage> message = chatMessageRepository.findById(chatMessage.getId());
        if(message.isPresent()) {
            Chat chat = chatRepository.findById(chatMessage.getChat().getId()).get();
            //no need to use ChatMessageRepository because of Casecad.All
            chat.getMessages().remove(message.get());
        }
    }

    private ChatMessageDTO convertToDTO(ChatMessage chatMessage) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(chatMessage.getId());
        dto.setMessage(chatMessage.getMessage());
        dto.setChatId(chatMessage.getChat().getId());
        dto.setSenderId(chatMessage.getSender().getId());
        return dto;
    }
}
