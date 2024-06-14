package com.example.demo.duel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DuelRequest {
    private Long challengerId;
    private Long challengedId;
}
