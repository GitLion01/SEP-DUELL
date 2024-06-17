import React, { createContext, useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
    const [client, setClient] = useState(null);
    const [game, setGame] = useState(null);
    const [users, setUsers] = useState([]);
    const [connected, setConnected] = useState(false);

    useEffect(() => {
        const userId = parseInt(localStorage.getItem('id')); // ID des aktuellen Benutzers als Zahl

        const newClient = new Client({
            brokerURL: 'ws://localhost:8080/game-websocket',
            webSocketFactory: () => new SockJS('http://localhost:8080/game-websocket'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket server');
                setConnected(true);

                // Wiederherstellung von Spiel und Benutzern aus dem Speicher
                const storedGame = sessionStorage.getItem('game');
                const storedUsers = sessionStorage.getItem('users');
                if (storedGame) {
                    setGame(JSON.parse(storedGame));
                }
                if (storedUsers) {
                    setUsers(JSON.parse(storedUsers));
                }

                // Subscribe für globale Herausforderung
                newClient.subscribe(`/user/${userId}/queue/create`, (message) => {
                    const response = JSON.parse(message.body);
                    console.log("Received response:", response)
                    setGame(response[0]);
                    setUsers(response[1]);

                    // Speichern des Spiels und der Benutzer im Speicher
                    sessionStorage.setItem('game', JSON.stringify(response[0]));
                    sessionStorage.setItem('users', JSON.stringify(response[1]));

                    if (response[0].id) {
                        localStorage.setItem('gameId', response[0].id);
                        window.dispatchEvent(new CustomEvent('gameCreated', { detail: response[0].id }));
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
        <WebSocketContext.Provider value={{ client, game, setGame, users, setUsers, connected }}>
            {children}
        </WebSocketContext.Provider>
    );
};
