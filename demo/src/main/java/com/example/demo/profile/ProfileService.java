package com.example.demo.profile;

import com.example.demo.decks.Deck;
import com.example.demo.game.GameRepository;
import com.example.demo.game.Statistic;
import com.example.demo.game.StatisticRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final GameRepository gameRepository;
    private final StatisticRepository statisticRepository;

    public ProfileService(ProfileRepository profileRepository, UserAccountRepository userAccountRepository, GameRepository gameRepository, StatisticRepository statisticRepository) {
        this.profileRepository = profileRepository;
        this.gameRepository = gameRepository;
        this.statisticRepository = statisticRepository;
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

    public List<Statistic> getHistory(Long id){
        UserAccount user = profileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return statisticRepository.findByUsername(user.getUsername());
    }



}
