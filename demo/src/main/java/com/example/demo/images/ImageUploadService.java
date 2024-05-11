package com.example.demo.images;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageUploadService {



    private final UserAccountRepository userAccountRepository;

    public ImageUploadService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public void updateImage(Long userId, MultipartFile image) throws IOException {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setImage(image.getBytes());
        userAccountRepository.save(user);
    }

}

