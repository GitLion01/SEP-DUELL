package com.example.demo.security.config;

import com.example.demo.user.UserAccountService;
import com.example.demo.user.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.http.HttpMethod;
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
                            req -> req.requestMatchers("/**")
                                      .permitAll()
                                      .anyRequest()
                                      .authenticated()
                ).formLogin(form -> form
                        .loginPage("/login")  // Angepasste Login-Seite, kann durch eine eigene ersetzt werden
                        .loginProcessingUrl("/perform_login")  // URL, auf der die Login-Anfrage verarbeitet wird
                        .defaultSuccessUrl("/home", true)  // Weiterleitungs-URL nach erfolgreichem Login
                        .failureUrl("/login?error=true")  // Weiterleitungs-URL nach fehlgeschlagenem Login
                        .usernameParameter("username")  // Benutzername-Parameter, standardmäßig ist es "username"
                        .passwordParameter("password")  // Passwort-Parameter, standardmäßig ist es "password"
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true"))
                .userDetailsService(userAccountService)
                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(userAccountService)
                .passwordEncoder(bCryptPasswordEncoder);
        return builder.build();
    }
}
