package com.example.demo.user;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class UserAccountService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final static String USER_NOT_FOUND_MESSAGE = "User not found";
    /*private final BCryptPasswordEncoder bCryptPasswordEncoder;*/

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userAccountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    public String signUpUser(UserAccount userAccount) {
        boolean userExists = userAccountRepository.findByEmail(userAccount.getEmail()).isPresent();

        if(userExists) {
            throw new IllegalStateException("email already taken.");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(userAccount.getPassword());
        userAccount.setPassword(encodedPassword);
        userAccountRepository.save(userAccount);

        //TODO: Send confirmation token

        return "it works";
    }
}
