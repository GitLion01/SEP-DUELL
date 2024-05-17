package com.example.demo.registration.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE ConfirmationToken c SET c.confirmedAt = ?2 WHERE c.token = ?1")
    int updateConfirmedAt(String token, LocalDateTime confirmedAt);

    @Transactional
    @Modifying
    @Query("DELETE FROM ConfirmationToken c WHERE c.appUser.email = ?1 AND c.purpose = ?2")
    void deleteTokensByEmailAndPurpose(String email, TokenPurpose purpose);

    @Query("SELECT count(c) > 0 FROM ConfirmationToken c WHERE c.appUser.id = :app_user_id AND c.token = :token")
    Boolean existsByUserIdAndToken(@Param("app_user_id") Long app_user_id, @Param("token") String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM ConfirmationToken c WHERE c.appUser.id = ?1 AND c.token = ?2")
    void deleteToken(Long app_user_id,String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM ConfirmationToken c WHERE c.expiresAt < ?1 AND c.confirmedAt IS NULL")
    void deleteExpiredTokens(LocalDateTime now);


}
