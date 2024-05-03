package com.example.demo.registration;

import com.example.demo.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private Date dateOfBirth;
    private byte[] image;
    private UserRole role;


}
