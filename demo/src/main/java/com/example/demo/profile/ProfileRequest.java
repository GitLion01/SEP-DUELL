package com.example.demo.profile;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ProfileRequest {
    private String name;
    private int SEPCoins;
    private int leaderboardPoints;
}
