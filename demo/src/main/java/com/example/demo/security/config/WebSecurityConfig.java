package com.example.demo.security.config;

import com.example.demo.user.UserAccountService;
import com.example.demo.user.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer :: disable)
                .authorizeHttpRequests(
                            req -> req
                                    /*// erlaubt alle GET-Anfragen sowohl für USER als auch für ADMIN
                                    .requestMatchers(HttpMethod.GET, "/**").permitAll()
                                    // beschränkt POST-Anfragen auf ADMIN (nur bezüglich der angegebenen URL)
                                    .requestMatchers(HttpMethod.POST, "/cards/upload").hasRole(UserRole.ADMIN.name())
                                    // beschränkt DELETE-Anfragen auf ADMIN (nur bezüglich der angegebenen URL)
                                    .requestMatchers(HttpMethod.DELETE, "/cards/delete/{name}").hasRole(UserRole.ADMIN.name())
                                    .anyRequest().authenticated()*/


                                    .requestMatchers("/**")
                                      .permitAll()
                                      .anyRequest()
                                      .authenticated()
                ).userDetailsService(userAccountService)
                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .build();
    }
}
