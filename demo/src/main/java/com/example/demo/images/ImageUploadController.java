package com.example.demo.images;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@CrossOrigin
@RestController
@RequestMapping("/image")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @Autowired
    public ImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/upload/{userId}")
    public ResponseEntity<String> uploadImage(@PathVariable Long userId, @RequestParam("image") MultipartFile image) {
        try {
            imageUploadService.updateImage(userId, image);
            return ResponseEntity.ok("Image uploaded successfully");
        } catch (IOException e) {
            e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        }
    }
}
