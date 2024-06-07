package com.example.demo.cards;

import com.example.demo.user.UserAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardInstanceRepository extends CrudRepository<CardInstance, Long> {
    List<CardInstance> findByUserAccount(UserAccount userAccount);

}
