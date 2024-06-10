import React, { createContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
    const [client, setClient] = useState(null);

    useEffect(() => {
        const newClient = new Client({
            brokerURL: 'ws://localhost:8080/game-websocket',
            webSocketFactory: () => new SockJS('http://localhost:8080/game-websocket'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket server');

                // Subscribe für globale Herausforderung
                newClient.subscribe('/all/create', (message) => {
                    const response = JSON.parse(message.body);
                    if (response.gameId) {
                        localStorage.setItem('gameId', response.gameId);
                        window.dispatchEvent(new CustomEvent('gameCreated', { detail: response.gameId }));
                    }
                });
            },
            onStompError: (frame) => {
                console.error(`Broker reported error: ${frame.headers['message']}`);
                console.error(`Additional details: ${frame.body}`);
            },
            onWebSocketError: (event) => {
                console.error('WebSocket error', event);
            },
            onWebSocketClose: (event) => {
                console.error('WebSocket closed', event);
            },
        });

        newClient.activate();
        setClient(newClient);

        return () => {
            if (newClient) {
                newClient.deactivate();
            }
        };

    }, []);

    return (
        <WebSocketContext.Provider value={{ client }}>
            {children}
        </WebSocketContext.Provider>
    );
};
