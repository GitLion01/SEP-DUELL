package com.example.demo.config;

import io.micrometer.common.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long gameID = extractGameIDFromSession(session);
        if (gameID != null) {
            sessions.computeIfAbsent(gameID, k -> new HashSet<>()).add(session);
            System.out.println("Verbindung hergestellt für Spiel " + gameID + ": " + session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Nachricht erhalten: " + message.getPayload());
        // Hier können wir eine Broadcast-Funktion aufrufen, wenn notwendig
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Verbindung geschlossen: " + session.getId());
    }

    Long extractGameIDFromSession(WebSocketSession session) {
        // Versuchen Sie, die gameID aus den Sitzungsattributen abzurufen
        Object gameIdObj = session.getAttributes().get("gameID");
        if (gameIdObj instanceof Long) {
            return (Long) gameIdObj;
        } else if (gameIdObj instanceof String) {
            try {
                return Long.parseLong((String) gameIdObj);
            } catch (NumberFormatException e) {
                // Fehler beim Parsen der gameID
                System.err.println("Fehler beim Parsen der gameID: " + e.getMessage());
                return null;
            }
        } else {
            // gameID ist nicht im richtigen Format in den Sitzungsattributen vorhanden
            return null;
        }
    }
}
