package com.example.demo.chat;

import com.example.demo.dto.UserDTO;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class ChatService {

    private final UserAccountRepository userAccountRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupRepository groupRepository;

    public ResponseEntity<Long> createChat(Long userId1,Long userId2) {
        Optional<UserAccount> user1 = userAccountRepository.findById(userId1);
        Optional<UserAccount> user2 = userAccountRepository.findById(userId2);
        if(user1.isPresent() && user2.isPresent())
        {
            UserAccount u1 = user1.get();
            UserAccount u2 = user2.get();

            boolean exists = false;
            for(Chat chat : u1.getUserChat())
            {
                if(chat.getUsers().contains(u2) && !(chat instanceof Group)) {
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
        return findeGemeinsameChat(userId1,userId2);
    }

    public ResponseEntity<Long> createGroup(List<Long> userIds,String groupName){
        Group group = new Group();
        group.setName(groupName);
        for(Long userId : userIds)
        {
            UserAccount userAccount = userAccountRepository.findById(userId).get();
            group.getUsers().add(userAccount);
            userAccount.getUserChat().add(group);
        }
        group =groupRepository.findById(groupRepository.save(group).getId()).get();
        return new ResponseEntity<>(group.getId(),HttpStatus.OK);
    }

    public void sendMessage(ChatMessage chatMessage) {
        try {
            for (Chat chat : chatRepository.findAll()) {
                if (Objects.equals(chatMessage.getChat().getId(), chat.getId()))
                {
                    chat.getMessages().add(chatMessage);
                    //we do not need to add it in the chatMessageRepository  it will be added automatically
                    chatRepository.save(chat);
                    chatMessage.setId(chat.getMessages().get(chat.getMessages().size() - 1).getId());
                    //or chatMessage = chatRepository.findeById(chatRepository.save(chat).getId()).get()

                    for(UserAccount userAccount : chat.getUsers())
                    {
                        messagingTemplate.convertAndSendToUser(userAccount.getId().toString(),"/queue/messages", convertToChatMessageDTO(chatMessage));
                    }
                    break;
                }
            }

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*public void sendGroupMessage(ChatMessage chatMessage) {
        try{
            for (Group group : groupRepository.findAll()) {
                if(Objects.equals(chatMessage.getChat().getId(), group.getId()))
                {
                    group.getMessages().add(chatMessage);
                    groupRepository.save(group);
                    chatMessage.setId(group.getMessages().get(group.getMessages().size() - 1).getId());
                    for(UserAccount userAccount : group.getUsers())
                    {
                        messagingTemplate.convertAndSendToUser(userAccount.getId().toString(),"/queue/messages", convertToChatMessageDTO(chatMessage));
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }*/

    public void editMessage(ChatMessage chatMessage) {
        ChatMessage existingMessage = chatMessageRepository.findById(chatMessage.getId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        existingMessage.setMessage(chatMessage.getMessage());
        chatMessageRepository.save(existingMessage);
        updateChatWithEditedMessage(existingMessage);

        for(UserAccount user : existingMessage.getChat().getUsers()) {
            messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/messages", convertToChatMessageDTO(existingMessage));
        }
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
            message.get().setMessage("");
            for(UserAccount user : chat.getUsers()) {
                messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/messages", convertToChatMessageDTO(message.get()));
            }
        }
    }

    private ChatMessageDTO convertToChatMessageDTO(ChatMessage chatMessage) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(chatMessage.getId());
        dto.setMessage(chatMessage.getMessage());
        dto.setChatId(chatMessage.getChat().getId());
        dto.setSenderId(chatMessage.getSender().getId());
        return dto;
    }

    private ResponseEntity<Long> findeGemeinsameChat(Long user1, Long user2){
        UserAccount userAccount1 = userAccountRepository.findById(user1).get();
        UserAccount userAccount2 = userAccountRepository.findById(user2).get();
        for(Chat chat : userAccount1.getUserChat())
        {
            if(chat.getUsers().contains(userAccount2) && !(chat instanceof Group))
                return new ResponseEntity<>(chat.getId(), HttpStatus.OK);
        }
        return new ResponseEntity<>((long) 0, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<List<GroupDTO>> getGroups(Long userId) {
       Optional<UserAccount> user = userAccountRepository.findById(userId);
       if(user.isPresent()) {
           UserAccount userAccount = user.get();
           List<Chat> chat = userAccount.getUserChat();
           List<GroupDTO> groups = new ArrayList<>();
           for(Chat getChat: chat)
           {
               if(getChat instanceof Group)
                   groups.add(convertToGroupDTO((Group) getChat));
           }
           return new ResponseEntity<>(groups, HttpStatus.OK);
       }
       return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    private GroupDTO convertToGroupDTO(Group group) {
        List<UserDTO> listUsers = new ArrayList<>();
        for(UserAccount userAccount : group.getUsers())
        {
            listUsers.add(new UserDTO(userAccount.getUsername(),userAccount.getId(),userAccount.getFirstName(),userAccount.getLastName()));
        }
        return new GroupDTO(group.getId(),group.getName(),listUsers);
    }

   public ResponseEntity<List<ChatMessageDTO>> getMessages(Long ChatId) {
        Chat chat = chatRepository.findById(ChatId).get();
        List<ChatMessageDTO> messagesDTO = new ArrayList<>();
        for(ChatMessage chatMessage : chat.getMessages())
            messagesDTO.add(convertToChatMessageDTO(chatMessage));
        return new ResponseEntity<>(messagesDTO, HttpStatus.OK);
   }
}
