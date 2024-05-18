package com.example.demo;

import com.example.demo.email.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class EmailTestController {
    private final EmailSender emailSender;

    @Autowired
    public EmailTestController(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @GetMapping("/send")
    public String sendTestEmail() {
        // Füge einen Betreff für die E-Mail hinzu
        String subject = "Test Email Subject";
        String content = "This is a test email sent by Spring Boot.";
        emailSender.send("gatekeeper.cardhaven@gmail.com", content, subject);
        return "Email sent!";
    }
}
