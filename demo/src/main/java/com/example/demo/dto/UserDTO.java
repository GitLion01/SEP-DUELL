package com.example.demo.dto;

public class UserDTO {

    private String username;

    // Konstruktoren, Getter und Setter
    public UserDTO() {
    }

    public UserDTO(String username) {
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
