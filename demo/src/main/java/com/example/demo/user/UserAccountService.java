package com.example.demo.user;
import com.example.demo.cards.Card;
import com.example.demo.cards.CardRepository;
import com.example.demo.cards.CardService;
import com.example.demo.registration.token.ConfirmationToken;
import com.example.demo.registration.token.ConfirmationTokenRepository;
import com.example.demo.registration.token.ConfirmationTokenService;
import com.example.demo.registration.token.TokenPurpose;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Service
public class UserAccountService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final static String USER_NOT_FOUND_MESSAGE = "User not found";
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final CardService cardService;
    private final CardRepository cardRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAccountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE));
}


    public boolean isUsernameTaken(String username) {
        return userAccountRepository.existsByUsername(username);
    }



    public String signUpUser(UserAccount userAccount) {
        boolean userExists = userAccountRepository.findByEmail(userAccount.getEmail()).isPresent();

        if(userExists) {
            throw new IllegalStateException("email already taken.");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(userAccount.getPassword());
        userAccount.setPassword(encodedPassword);
        userAccountRepository.save(userAccount);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                userAccount,
                TokenPurpose.REGISTRATION
        );
        confirmationTokenService.save(confirmationToken);

        return token;
    }

    public void enableAppUser(String email,Long userId) {
        //give the user if confirmed all the cards from admin panel
        List<Card> cards= cardRepository.findAll();
        for(Card card:cards) {
            cardService.addCardsInstanzen(userId, Collections.singletonList(card.getName()));
        }
        userAccountRepository.enableAppUser(email);
    }

    @Transactional
    public void deleteUser(String email) {
        if(email != null && userAccountRepository.findByEmail(email).isPresent()) {
            confirmationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            userAccountRepository.deleteByEmail(email);
        }
    }

}
