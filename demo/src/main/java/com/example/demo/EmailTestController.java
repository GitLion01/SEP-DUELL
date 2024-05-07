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
        emailSender.send("gatekeeper.cardhaven@web.de", "This is a test email");
        return "Email sent!";
    }
}
