package com.example.demo.security.config;

import com.example.demo.user.UserAccountService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {


    private final UserAccountService userAccountService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer :: disable)
                .authorizeHttpRequests(
                            req -> req.requestMatchers("/registration/**", "/registration")
                                      .permitAll()
                                      .anyRequest()
                                      .authenticated()
                ).userDetailsService(userAccountService)
                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .build();
    }
}
