package com.example.demo.registration;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/registration")
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
    public String getStatus(){
        return "Registration Service is up and running but no one knows why or how";
    }

    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }
}
