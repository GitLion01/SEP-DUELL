package com.example.demo.decks;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Getter
public class DeckRequest {
    private Long userID;
    private String name;
    private List<String> cardNames;
}

