package com.example.demo.chat;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin
@RestController
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatRepository chatRepository;

    @PostMapping("/create.chat")
    public ResponseEntity<Long> createChat(@RequestParam Long userId1, @RequestParam Long userId2)
    {
        return chatService.createChat(userId1,userId2);
    }

    @PostMapping("/create.group")
    public ResponseEntity<Long> createGroup(@RequestBody List<Long> userIds, @RequestParam String groupName){
        return chatService.createGroup(userIds,groupName);
    }

    @GetMapping("/get.messages")
    public ResponseEntity<List<ChatMessageDTO>> getChat(@RequestParam Long chatId,@RequestParam Long userID)
    {
        return chatService.getMessages(chatId,userID);
    }

    @GetMapping("/get.groups")
    public ResponseEntity<List<GroupDTO>> getGroups(@RequestParam Long userId)
    {
        return chatService.getGroups(userId);
    }


    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage)
    {
        System.out.println("Start sendMessage");
        System.out.println("Received message: " + chatMessage.getMessage());
        chatService.checkOnline(chatMessage);
    }

    @MessageMapping("/onChat")
    public void onChat(@Payload ChatMessage chatMessage,@Header("userId") String userIdHeader,@Header("chatId") String chatIdHeader)
    {
        System.out.println("-----------------------------------------------------");
        Long userId =  Long.parseLong(userIdHeader);
        Long chatId = Long.parseLong(chatIdHeader);
        System.out.println("Processing onChat for userId: " + userId + " and chatId: " + chatId);
        System.out.println("Received message in onChat: " + chatMessage.getMessage());
        chatService.sendMessage(chatMessage,userId,chatId);
    }

    /*@MessageMapping("/send-group-message")
    public void sendGroupMessage(@Payload ChatMessage chatMessage)
    {
        chatService.sendGroupMessage(chatMessage);
    }*/

    @MessageMapping("/editMessage")
    public void editMessage(@Payload ChatMessage chatMessage)
    {
        chatService.editMessage(chatMessage);
    }

    @MessageMapping("/deleteMessage")
    public void deleteMessage(@Payload ChatMessage chatMessage)
    {
        chatService.deleteMessage(chatMessage);
    }

}
