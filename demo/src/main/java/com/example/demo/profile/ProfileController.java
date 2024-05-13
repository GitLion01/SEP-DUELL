package com.example.demo.profile;


import com.example.demo.decks.Deck;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/profile")
@CrossOrigin
@AllArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping(path ="/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        Optional<UserAccount> profileOptional = profileService.getProfile(id);
        if (profileOptional.isPresent()) {
            UserAccount profile = profileOptional.get();

            // Konvertieren Sie das Bild in einen Base64-String
            byte[] image = profile.getImage();
            String imageBase64 = null;
            if(image != null) {
                imageBase64 = Base64.getEncoder().encodeToString(profile.getImage());
            }
            //Abrufen der Decks des Users
            List<Deck> decks = profileService.getUserDecks(id);

            // Erstellen Sie ein UserAccountResponse-Objekt mit den Profilinformationen und dem Base64-String des Bildes
            UserAccountResponse profileResponse = new UserAccountResponse(
                    profile.getFirstName(),
                    profile.getLastName(),
                    profile.getUsername(),
                    profile.getEmail(),
                    profile.getDateOfBirth(),
                    profile.getLeaderboardPoints(),
                    profile.getSepCoins(),
                    imageBase64,
                    profile.getRole(),
                    profile.getLocked(),
                    profile.getEnabled(),
                    decks
            );
            return ResponseEntity.ok(profileResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
