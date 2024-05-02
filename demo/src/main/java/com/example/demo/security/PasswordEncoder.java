package com.example.demo.security;

import org.springframework.context.annotation.Bean;                                 //ö
import org.springframework.context.annotation.Configuration;                //ö
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;            //ö

@Configuration
public class PasswordEncoder {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
