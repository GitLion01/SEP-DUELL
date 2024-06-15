package com.example.demo.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

    private Long id;
    private String message;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private boolean read;
    private String onChat;
}
