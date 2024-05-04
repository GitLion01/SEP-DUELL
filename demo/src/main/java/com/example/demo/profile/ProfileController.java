package com.example.demo.profile;


import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(path = "/profile")
@CrossOrigin
@AllArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping(path ="/{id}")
    public ResponseEntity<?> getProfile(@PathVariable int id) {
        Optional<UserAccount> profileOptional = profileService.getProfile(id);
        if (profileOptional.isPresent()) {
            return ResponseEntity.ok(profileOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
