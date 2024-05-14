package com.example.demo.registration;

import com.example.demo.user.UserRole;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Getter
@Setter
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
