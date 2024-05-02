package com.example.demo.registration;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/registration")
@CrossOrigin
@AllArgsConstructor
public class RegistrationController {


    private final RegistrationService registrationService;
    private final UserAccountRepository userAccountRepository;

    @PostMapping
   /* @CrossOrigin*/
    public String register(@RequestBody RegistrationRequest request) {
        return registrationService.register(request);
    }

    @GetMapping(path = "/users")
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    @GetMapping
    public String getStatus() {
        // Hier kannst du eine Methode aus deinem RegistrationService aufrufen, die den gewünschten Status zurückgibt
        // Zum Beispiel könnte diese Methode prüfen, ob der Service verfügbar ist oder andere relevante Informationen zurückgeben
        return "Registration service is up and running!";
    }

    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }
}
