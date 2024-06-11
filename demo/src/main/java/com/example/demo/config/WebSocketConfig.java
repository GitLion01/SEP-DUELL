package com.example.demo.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {





    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/all", "/user","/specific"); // Nachrichten werden an /all oder an /specific gesendet (Base URL für ausgehende Antworten)
        config.setApplicationDestinationPrefixes("/app"); // Präfix für eingehende Nachrichten (Base URL für eingehende Anfragen)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/game-websocket").setAllowedOrigins("http://localhost:4000", "http://localhost:3000", "http://localhost:4005", "http://localhost:4001", "http://localhost:63342");// Endpunkte für WebSocket-Verbindung
        registry.addEndpoint("/game-websocket").setAllowedOrigins("http://localhost:4000", "http://localhost:3000", "http://localhost:4005", "http://localhost:4001", "http://localhost:63342").withSockJS(); // falls Browser kein websocket unterstützt
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> {
            if (handler instanceof MyWebSocketHandler) {
                return new MyWebSocketHandler() { // Anonyme Klasse
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        // Hier die gameID aus der WebSocket-Sitzung abrufen
                        Long gameID = extractGameIDFromSession(session);
                        // Fügen Sie die gameID der Sitzung hinzu, um später darauf zuzugreifen
                        session.getAttributes().put("gameID", gameID);
                        // Aufrufen der ursprünglichen Methode in MyWebSocketHandler
                        super.afterConnectionEstablished(session);
                    }
                };
            } else {
                return handler;
            }
        });
    }




}




/*import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MyWebSocketHandler(), "/websocket").setAllowedOrigins("*");
    }
}*/
