package com.example.demo.login;

import com.example.demo.email.EmailSender;
import com.example.demo.leaderboard.LeaderboardService;
import com.example.demo.registration.token.ConfirmationToken;
import com.example.demo.registration.token.ConfirmationTokenRepository;
import com.example.demo.registration.token.ConfirmationTokenService;
import com.example.demo.registration.token.TokenPurpose;
import com.example.demo.user.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController

@RequestMapping("/login")
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    @Autowired
    private LeaderboardService leaderboardService;

    // Supercode als Konstante definieren
    private static final String SUPER_CODE = "SUPER1";
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    public LoginController(AuthenticationManager authenticationManager,
                           ConfirmationTokenService confirmationTokenService,
                           EmailSender emailSender, ConfirmationTokenRepository confirmationTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.confirmationTokenService = confirmationTokenService;
        this.emailSender = emailSender;
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserAccount userAccount = (UserAccount) authentication.getPrincipal();

            leaderboardService.updateUserStatus(userAccount.getId(), "online");

            String token = confirmationTokenService.generateToken(userAccount, TokenPurpose.LOGIN);
            String subject = "Login Verification";  // Änderung
            emailSender.send(userAccount.getEmail(), buildLoginEmail(userAccount.getFirstName(), token), subject);

            return ResponseEntity.ok(Map.of("status", "success",
                    "message", "Login successful. Please check your email for the verification code.",
                    "id", String.valueOf(userAccount.getId()),
                    "userRole",String.valueOf(userAccount.getRole())
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public String verifyLoginToken(@RequestBody LoginTokenRequest request) {
        // Überprüfung auf den Supercode
        if (request.getToken().equals(SUPER_CODE)) {
            confirmationTokenRepository.deleteToken(request.getUserId(), request.getToken());
            return "Login verified successfully with Super Code";
        }

        return confirmationTokenService.getToken(request.getToken())
                .map(token -> {
                    if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return "Token expired";
                    } else if (token.getPurpose() != TokenPurpose.LOGIN) {
                        return "Invalid token purpose";
                    }else if (!confirmationTokenRepository.existsByUserIdAndToken(request.getUserId(), request.getToken())) {
                        return "Token does not match user ID " + request.getUserId();
                    }else {
                        confirmationTokenRepository.deleteToken(request.getUserId(), request.getToken());
                        return "Login verified successfully";
                    }
                })
                .orElse("Invalid token");
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Long userId) {
        leaderboardService.updateUserStatus(userId, "offline");
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("status", "success", "message", "Logout successful."));
    }

    private String buildLoginEmail(String name, String token) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "Hi " + name + ",\n\n" +
                "Here is your verification code for login: <strong>" + token + "</strong>\n\n" +
                "The code will expire in 15 minutes.\n" +
                "See you soon!";
    }
}