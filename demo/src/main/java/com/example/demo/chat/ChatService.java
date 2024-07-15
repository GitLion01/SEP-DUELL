package com.example.demo.chat;

import com.example.demo.clan.Clan;
import com.example.demo.clan.ClanRepository;
import com.example.demo.dto.UserDTO;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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
    private final ClanRepository clanRepository;


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

    public void sendMessage(ChatMessage chatMessage,Long userId,Long chatId) {
        try {
            for (Chat chat : chatRepository.findAll()) {
                if (Objects.equals(chatMessage.getChat().getId(), chat.getId()))
                {
                    if(!chatMessage.getSender().getId().equals(userId) && chatMessage.getChat().getId().equals(chatId))
                        chatMessage.setRead(true);
                    chatMessage.getSender().setUsername(userAccountRepository.findById(chatMessage.getSender().getId()).get().getUsername());
                    chatMessageRepository.save(chatMessage);
                    System.out.println("sent message");
                    System.out.println(convertToChatMessageDTO(chatMessage));
                    messagingTemplate.convertAndSendToUser(userId.toString(),"/queue/messages", convertToChatMessageDTO(chatMessage));
                    System.out.println("sent message 2");
                    break;
                }
            }

        }
        catch (Exception e) {
            System.out.println("Error saving chat message: " + e.getMessage());
        }
    }


    public void editMessage(ChatMessage chatMessage) {
        ChatMessage existingMessage = chatMessageRepository.findById(chatMessage.getId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if(!existingMessage.isRead()) {
            existingMessage.setMessage(chatMessage.getMessage());
            chatMessageRepository.save(existingMessage);
            updateChatWithEditedMessage(existingMessage);

            for(Clan clan : clanRepository.findAll()){
                if(Objects.equals(clan.getGroup().getId(), chatMessage.getChat().getId()))
                    for(UserAccount user : clan.getUsers()){
                        messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/messages", convertToChatMessageDTO(existingMessage));
                    }
            }

            for (UserAccount user : existingMessage.getChat().getUsers()) {
                messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/messages", convertToChatMessageDTO(existingMessage));
            }
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
            if(!message.get().isRead()) {
                Chat chat = chatRepository.findById(chatMessage.getChat().getId()).get();
                //no need to use ChatMessageRepository because of Casecad.All
                chat.getMessages().remove(message.get());
                message.get().setMessage("");

                for(Clan clan : clanRepository.findAll()){
                    if(Objects.equals(clan.getGroup().getId(), chatMessage.getChat().getId()))
                        for(UserAccount user : clan.getUsers()){
                            messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/messages", convertToChatMessageDTO(message.get()));
                        }
                }

                for (UserAccount user : chat.getUsers()) {
                    messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/messages", convertToChatMessageDTO(message.get()));
                }
            }
        }
    }

    private ChatMessageDTO convertToChatMessageDTO(ChatMessage chatMessage) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(chatMessage.getId());
        dto.setMessage(chatMessage.getMessage());
        dto.setChatId(chatMessage.getChat().getId());
        dto.setSenderId(chatMessage.getSender().getId());
        dto.setSenderName(chatMessage.getSender().getUsername());
        dto.setRead(chatMessage.isRead());
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

    public ResponseEntity<List<ChatMessageDTO>> getMessages(Long chatId,Long userID) {
        Chat chat = chatRepository.findById(chatId).get();
        List<ChatMessageDTO> messagesDTO = new ArrayList<>();
        setReadTrue(chatId,userID);
        for(ChatMessage chatMessage : chat.getMessages())
            messagesDTO.add(convertToChatMessageDTO(chatMessage));
        return new ResponseEntity<>(messagesDTO, HttpStatus.OK);
   }

    public void setReadTrue(Long chatId,Long userID){
        Chat chat = chatRepository.findById(chatId).get();
        for(ChatMessage chatMessage : chat.getMessages())
        {
            if(Objects.equals(chatMessage.getSender().getId(), userID))
                continue;
            chatMessage.setRead(true);
        }
    }

    public void checkOnline(ChatMessage chatMessage) {
        Chat chat = chatRepository.findById(chatMessage.getChat().getId()).get();
        System.out.println("chat id is"+chat.getId());

        if(!chat.getMessages().contains(chatMessage)) {
            chat.getMessages().add(chatMessage);
            //we do not need to add it in the chatMessageRepository  it will be added automatically
            chatRepository.save(chat);
            chatMessage.setId(chat.getMessages().get(chat.getMessages().size() - 1).getId());
            //or chatMessage = chatRepository.findeById(chatRepository.save(chat).getId()).get()
        }
        List<UserAccount> users =chat.getUsers();

        //der Fall dass es ein Clan Nachricht
        if(users.isEmpty()){
            try {
                UserAccount user = userAccountRepository.findById(chatMessage.getSender().getId()).get();
                Clan clan = user.getClan();
                users = clan.getUsers();
            }catch(Exception e) {}
        }

        for(UserAccount user : users) {
            if(user.getId().equals(chatMessage.getSender().getId()))
                continue;
            messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/messages", convertToChatMessageDTO2(chatMessage));
        }
        messagingTemplate.convertAndSendToUser(chatMessage.getSender().getId().toString(), "/queue/messages", convertToChatMessageDTO2(chatMessage));

    }

    private ChatMessageDTO convertToChatMessageDTO2(ChatMessage chatMessage) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(chatMessage.getId());
        dto.setMessage(chatMessage.getMessage());
        dto.setChatId(chatMessage.getChat().getId());
        dto.setSenderId(chatMessage.getSender().getId());
        dto.setSenderName(chatMessage.getSender().getUsername());
        dto.setRead(chatMessage.isRead());
        dto.setOnChat("on Chat");
        return dto;
    }

    public ResponseEntity<GroupDTO> getClanChat(Long userId) {
        UserAccount userAccount = userAccountRepository.findById(userId).get();
        if(userAccount.getClan()!=null)
            return ResponseEntity.ok(convertToGroupDTO(userAccount.getClan().getGroup()));
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}
