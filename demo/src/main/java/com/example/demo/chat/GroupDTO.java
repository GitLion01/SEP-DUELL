package com.example.demo.chat;

import com.example.demo.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class GroupDTO {
    private Long id;
    private String name;
    private List<UserDTO> users;
}