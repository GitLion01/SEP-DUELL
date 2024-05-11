package com.example.demo.registration;

import com.example.demo.images.ImageUploadService;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import com.example.demo.user.UserRole;
import jakarta.servlet.Registration;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/registration")
@CrossOrigin
@AllArgsConstructor
public class RegistrationController {


    private final RegistrationService registrationService;
    private final UserAccountRepository userAccountRepository;




   /* @PostMapping
    public String register(@RequestBody RegistrationRequest request) {
        return registrationService.register(request);
    }*/

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String register(
            @RequestParam("image") MultipartFile image,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("dateOfBirth") String dateOfBirthStr,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") UserRole role
    ) {
        try {
            // Convert date string to Date object
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateOfBirth = dateFormat.parse(dateOfBirthStr);

            // Convert role string to UserRole enum
            /*UserRole role = UserRole.valueOf(roleStr.toUpperCase());*/

            byte[] imageData = image.getBytes();
            return registrationService.register(imageData, firstName, lastName, dateOfBirth, username, email, password, role);
        } catch (IOException | ParseException | IllegalArgumentException e) {
            e.printStackTrace();
            return "Fehler beim Lesen des Bildes oder beim Parsen der Daten";
        }
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
