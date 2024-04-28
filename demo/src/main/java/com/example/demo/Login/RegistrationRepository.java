package com.example.demo.Login;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface RegistrationRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);

}
