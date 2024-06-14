// src/main/java/com/example/demo/leaderboard/StatusUpdate.java
package com.example.demo.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdate {
    private Long userId;
    private String status;
}
