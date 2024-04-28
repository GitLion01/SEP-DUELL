package com.example.demo.Login;

import org.springframework.security.core.GrantedAuthority;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Player extends UserAccount{

    public Player() {

    }

    public Player(String firstName,
                  String lastName,
                  String email,
                  String password,
                  Date dateOfBirth,
                  byte[] image) {

        super.setFirstName(firstName);
        super.setLastName(lastName);
        super.setEmail(email);
        super.setPassword(password);
        super.setDateOfBirth(dateOfBirth);
        super.setImage(image);
    }

    @Override
    public void viweProfile(Integer userID) {

    }

    @Override
    public void login(String email, String password) {

    }


}
