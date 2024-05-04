package com.example.demo.profile;
import com.example.demo.user.UserAccount;
import org.springframework.data.repository.CrudRepository;

public interface ProfileRepository extends CrudRepository<UserAccount, Long> {
    UserAccount findById(int id);
}
