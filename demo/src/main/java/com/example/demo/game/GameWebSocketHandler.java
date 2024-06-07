package com.example.demo.game;
import com.example.demo.game.requests.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameService gameService;

    @Autowired
    public GameWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("New connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Message received: " + payload);

        // Deserialize JSON to the appropriate request class
        BaseRequest baseRequest = objectMapper.readValue(payload, BaseRequest.class);

        switch (baseRequest.getRequestType()) {
            case "JOIN":
                JoinRequest joinRequest = objectMapper.readValue(payload, JoinRequest.class);
                Game game = gameService.joinGame(joinRequest);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(game)));
                break;
            case "SELECT_DECK":
                DeckSelectionRequest deckSelectionRequest = objectMapper.readValue(payload, DeckSelectionRequest.class);
                ResponseEntity<Game> deckResponse = gameService.selectDeck(deckSelectionRequest);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(deckResponse.getBody())));
                break;
            case "PLAY_CARD":
                PlayCardRequest playCardRequest = objectMapper.readValue(payload, PlayCardRequest.class);
                ResponseEntity<Game> playResponse = gameService.playCard(playCardRequest);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(playResponse.getBody())));
                break;
            case "END_TURN":
                EndTurnRequest endTurnRequest = objectMapper.readValue(payload, EndTurnRequest.class);
                ResponseEntity<Game> endTurnResponse = gameService.endTurn(endTurnRequest);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(endTurnResponse.getBody())));
                break;
            case "ATTACK":
                AttackRequest attackRequest = objectMapper.readValue(payload, AttackRequest.class);
                ResponseEntity<Game> attackResponse = gameService.attackCard(attackRequest);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(attackResponse.getBody())));
                break;
            // Handle other request types similarly
            default:
                throw new IllegalArgumentException("Unknown request type: " + baseRequest.getRequestType());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection closed: " + session.getId());
    }
}

