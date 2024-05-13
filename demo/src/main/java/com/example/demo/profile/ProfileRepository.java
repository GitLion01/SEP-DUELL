package com.example.demo.profile;
import com.example.demo.decks.Deck;
import com.example.demo.user.UserAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfileRepository extends CrudRepository<UserAccount, Long> {
    @Query("select d from Deck d where d.user.id = ?1")
    List<Deck> findDecksByUserId(@Param("userId") Long userId);
}
