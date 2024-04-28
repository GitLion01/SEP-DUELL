package com.example.demo.Login;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class RegistrationService {


    private final RegistrationRepository registrationRepository;

    public RegistrationService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public List<UserAccount> getAllUsers() {
        return registrationRepository.findAll();
    }

    public String register(RegistrationRequest request){
        if(request.getIsAdmin()){
            Admin admin = new Admin();
            admin.setFirstName(request.getFirstName());
            admin.setLastName(request.getLastName());
            admin.setEmail(request.getEmail());
            admin.setPassword(request.getPassword());
            admin.setDateOfBirth(request.getDateOfBirth());
            admin.setImage(request.getImage());
            registrationRepository.save(admin);
            return "Registration Successful";
        }else{
            Player player = new Player();
            player.setFirstName(request.getFirstName());
            player.setLastName(request.getLastName());
            player.setEmail(request.getEmail());
            player.setPassword(request.getPassword());
            player.setDateOfBirth(request.getDateOfBirth());
            player.setImage(request.getImage());
            registrationRepository.save(player);
            return "Registration Successful";
        }
    }
}
