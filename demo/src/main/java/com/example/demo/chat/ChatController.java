package com.example.demo.chat;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin
@RestController
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/create.chat")
    public void createChat(@RequestParam Long userId1, @RequestParam Long userId2)
    {
        chatService.createChat(userId1,userId2);
    }

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage)
    {
        chatService.sendMessage(chatMessage);
    }

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
