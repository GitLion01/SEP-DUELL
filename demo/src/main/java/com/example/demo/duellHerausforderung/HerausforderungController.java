package com.example.demo.duellHerausforderung;


import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin
public class HerausforderungController {
    private final HerausforderungService herausforderungService;

    @MessageMapping("/send.herausforderung")
    public void sendHerausforderung(@Header Long senderId,@Header Long receiverId){
        herausforderungService.sendHerausforderung(senderId,receiverId);
    }

    @MessageMapping("/accept.herausforderung")
    public void acceptHerausforderung(@Header Long senderId,@Header Long receiverId){
        herausforderungService.acceptHerausforderung(senderId,receiverId);
    }

}
