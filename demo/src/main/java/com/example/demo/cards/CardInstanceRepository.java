package com.example.demo.cards;

import com.example.demo.user.UserAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardInstanceRepository extends CrudRepository<CardInstance, Long> {
    List<CardInstance> findByUserAccount(UserAccount userAccount);
}
