package com.example.demo.profile;

import com.example.demo.user.UserAccount;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Optional<UserAccount> getProfile(int id)
    {
        return profileRepository.findById(id);
    }
}
