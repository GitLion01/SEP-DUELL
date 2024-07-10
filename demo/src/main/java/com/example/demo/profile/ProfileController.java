package com.example.demo.profile;


import com.example.demo.decks.Deck;
import com.example.demo.game.GameService;
import com.example.demo.game.Statistic;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> getProfile(@PathVariable Long id) {// Verwendung der Wildcard, da entweder UserAccountResponse oder ResponseEntity.notFound().build() zurückgegeben wird
        Optional<UserAccount> profileOptional = profileService.getProfile(id);
        if (profileOptional.isPresent()) {
            UserAccount profile = profileOptional.get();
            String clanName="";
            if(profile.getClan()!=null)
                clanName = profile.getClan().getName();
            // Konvertiert das Bild in einen Base64-String
            byte[] image = profile.getImage();
            String imageBase64 = null;
            if(image != null) {
                imageBase64 = Base64.getEncoder().encodeToString(profile.getImage());
            }
            //Abrufen der Decks des Users
            List<Deck> decks = profileService.getUserDecks(id);

            // Erstellt ein UserAccountResponse-Objekt mit den Profilinformationen und dem Base64-String des Bildes
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
                    decks,
                    clanName
            );
            return ResponseEntity.ok(profileResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(path ="/{id}")
    public ResponseEntity<String> updateSEPCoins(@PathVariable Long id,@RequestParam("sepCoins") Integer sepCoins) {
        String result = profileService.updateSEPCoins(sepCoins,id);
        HttpStatus status = result.startsWith("fail") ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return new ResponseEntity<>(result, status);
    }

    @GetMapping(path = "/history/{id}")
    public List<Statistic> getHistory(@PathVariable Long id) {
        return profileService.getHistory(id);
    }
}
