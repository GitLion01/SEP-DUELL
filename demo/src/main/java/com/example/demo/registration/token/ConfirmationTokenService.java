package com.example.demo.registration.token;

import com.example.demo.user.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    public String generateToken(UserAccount userAccount, TokenPurpose purpose) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 6);;
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                userAccount,
                purpose
        );
        confirmationTokenRepository.deleteTokensByEmailAndPurpose(userAccount.getEmail(), purpose);
        confirmationTokenRepository.save(confirmationToken);
        return token;
    }

    public void save(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public int setConfirmedAt(String token) {
        return confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }
}
