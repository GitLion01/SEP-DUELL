package com.example.demo.profile;

import com.example.demo.decks.Deck;
import com.example.demo.user.UserAccount;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Optional<UserAccount> getProfile(Long id)
    {
        return profileRepository.findById(id);
    }

    public List<Deck> getUserDecks(Long userId) {
        return profileRepository.findDecksByUserId(userId);
    }

    public String updateSEPCoins(Integer coins, Long userId) {
        Optional<UserAccount> user = profileRepository.findById(userId);
        if(user.isPresent())
        {
            UserAccount userGet = user.get();
            userGet.setSepCoins(coins);
            profileRepository.save(userGet);
            return "success";
        }
        return "fail";
    }



}
