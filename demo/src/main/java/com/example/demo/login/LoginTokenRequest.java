package com.example.demo.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LoginTokenRequest {

    private String token;
    private Long userId;

}
