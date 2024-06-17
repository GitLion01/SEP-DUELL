package com.example.demo.duellHerausforderung;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Notification {
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String message;
}
