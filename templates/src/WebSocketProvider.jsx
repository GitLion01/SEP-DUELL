import React, { createContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
    const [client, setClient] = useState(null);
    const [game, setGame] = useState(null);
    const [users, setUsers] = useState([]);

    useEffect(() => {

        const userId = parseInt(localStorage.getItem('id')); // ID des aktuellen Benutzers als Zahl


        const newClient = new Client({
            brokerURL: 'ws://localhost:8080/game-websocket',
            webSocketFactory: () => new SockJS('http://localhost:8080/game-websocket'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket server');

                // Subscribe für globale Herausforderung
                newClient.subscribe(`/user/${userId}/queue/create`, (message) => {
                    const response = JSON.parse(message.body);
                    console.log("Received response:", response)

                    // Finde den Benutzer im Array
                    // const userInGame = response.users.find(user => user.id === userId);

                    if (response.id) {
                        localStorage.setItem('gameId', response.id);
                        window.dispatchEvent(new CustomEvent('gameCreated', { detail: response.id }));
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
        <WebSocketContext.Provider value={{ client, game, setGame, users, setUsers }}>
            {children}
        </WebSocketContext.Provider>
    );
};
