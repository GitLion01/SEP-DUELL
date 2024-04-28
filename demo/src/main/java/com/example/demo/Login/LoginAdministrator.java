package com.example.demo.Login;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path = "user/registration")
@AllArgsConstructor
public class LoginAdministrator {



    /*private Integer verificationCode;*/
    private RegistrationService registrationService;

    @PostMapping(path = "user/registration")
    public String register(@RequestBody RegistrationRequest request) {
        return registrationService.register(request);
    }

    @GetMapping(path = "/users")
    public List<UserAccount> getAllUsers() {
        return registrationService.getAllUsers();
    }






}
