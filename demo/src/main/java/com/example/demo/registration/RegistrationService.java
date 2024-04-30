package com.example.demo.registration;


import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountService;
import com.example.demo.user.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class RegistrationService {

    private final EmailValidator emailValidator;
    private final UserAccountService userAccountService;


    public String register(RegistrationRequest request) {
        boolean isValidemail = emailValidator.test(request.getEmail());
        if(!isValidemail) {
            throw new IllegalStateException("Invalid email");
        }
        return userAccountService.signUpUser(new UserAccount(request.getFirstName(), request.getLastName(), request.getDateOfBirth(), request.getEmail(), request.getPassword(), UserRole.USER));
    }


}
