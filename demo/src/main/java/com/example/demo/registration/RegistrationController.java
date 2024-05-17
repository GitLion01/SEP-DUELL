package com.example.demo.registration;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import com.example.demo.user.UserAccountService;
import com.example.demo.user.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path = "/registration")
@CrossOrigin
@AllArgsConstructor
public class RegistrationController {


    private final RegistrationService registrationService;
    private final UserAccountRepository userAccountRepository;






    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> register(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("dateOfBirth") String dateOfBirthStr,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") boolean isAdmin
    ) {
        try {
            // Convert date string to Date object
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateOfBirth = dateFormat.parse(dateOfBirthStr);

            UserRole role = isAdmin ? UserRole.ADMIN : UserRole.USER;

            /*byte[] imageData = image.getBytes();*/
            byte[] imageData = null;
            if(image != null){
                imageData = image.getBytes();
            }
            String registrationResult = registrationService.register(imageData, firstName, lastName, dateOfBirth, username, email, password, role);

            // Based on the registration result, construct and return the appropriate ResponseEntity
            if (registrationResult.startsWith("Error")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registrationResult);
            } else {
                return ResponseEntity.ok(registrationResult);
            }
        } catch (IOException | ParseException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Lesen des Bildes oder beim Parsen der Daten");
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
