package com.example.demo.duel;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DuelService {

    private final UserAccountRepository userAccountRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void challengeUser(Long challengerId, Long challengedId) {
        UserAccount challenger = userAccountRepository.findById(challengerId)
                .orElseThrow(() -> new IllegalArgumentException("Challenger not found"));
        UserAccount challenged = userAccountRepository.findById(challengedId)
                .orElseThrow(() -> new IllegalArgumentException("Challenged not found"));

        if (!challenger.canChallenge(challenged)) {
            throw new IllegalStateException("Challenge conditions not met");
        }

        challenged.setDuelStatus("challenged");
        challenged.setChallenger(challenger); // Setzen des Herausforderers
        challenger.setDuelStatus("challenging");

        userAccountRepository.save(challenger);
        userAccountRepository.save(challenged);

        messagingTemplate.convertAndSendToUser(challenged.getUsername(), "/queue/duel-challenge", challenger);
    }

    @Transactional
    public void respondToChallenge(Long challengedId, boolean accepted) {
        UserAccount challenged = userAccountRepository.findById(challengedId)
                .orElseThrow(() -> new IllegalArgumentException("Challenged not found"));
        UserAccount challenger = challenged.getChallenger();

        if (accepted) {
            challenged.setDuelStatus("in_duel");
            challenger.setDuelStatus("in_duel");

            messagingTemplate.convertAndSendToUser(challenger.getUsername(), "/queue/duel-accepted", challenged);
            messagingTemplate.convertAndSendToUser(challenged.getUsername(), "/queue/duel-accepted", challenger);
        } else {
            challenged.setDuelStatus("available");
            challenged.setChallenger(null); // Entfernen des Herausforderers
            challenger.setDuelStatus("available");

            messagingTemplate.convertAndSendToUser(challenger.getUsername(), "/queue/duel-declined", challenged);
        }

        userAccountRepository.save(challenger);
        userAccountRepository.save(challenged);
    }
}
