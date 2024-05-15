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
        String recipient = "gatekeeper.cardhaven@web.de";  // Empfänger der E-Mail
        String emailContent = "This is a test email";  // Inhalt der E-Mail
        String emailSubject = "Test Email Subject";  // Betreff der E-Mail

        emailSender.send(recipient, emailContent, emailSubject);  // Füge den Betreff hinzu
        return "Email sent!";
    }
}

