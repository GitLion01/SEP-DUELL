import React, { createContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { toast } from 'react-toastify';


export const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
    const [client, setClient] = useState(null);
    const [chatClient, setChatClient] = useState(null); 
    const [notifications, setNotifications] = useState([]); 
    const navigate = useNavigate(); 
    const userId = parseInt(localStorage.getItem('id'))

    useEffect(() => {



        const newClient = new Client({
            brokerURL: 'ws://localhost:8080/game-websocket',
            webSocketFactory: () => new SockJS('http://localhost:8080/game-websocket'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket server');
                // Subscribe für Benachrichtigung 
                newClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
                    const notification = JSON.parse(message.body);
                    if (notification.message === 'challenge') {
                        setNotifications(prev => [...prev, { ...notification, countdown: 30 }]);
                    } else if (notification.type === 'duelAccepted') {
                        // Weitere Logik für akzeptierte Duelle
                    }

                });

                // Subscribe für globale Herausforderung
                newClient.subscribe(`/user/${userId}/queue/create`, (message) => {
                    const response = JSON.parse(message.body);
                    console.log("Received response:", response)

                    // Finde den Benutzer im Array
                    // const userInGame = response.users.find(user => user.id === userId);

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
        
        const newChatClient = new Client({
            brokerURL: 'ws://localhost:8080/chat',
            webSocketFactory: () => new SockJS('http://localhost:8080/chat'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to chat WebSocket server');
            },
            onStompError: (frame) => {
                console.error(`Chat broker reported error: ${frame.headers['message']}`);
                console.error(`Additional details: ${frame.body}`);
            },
            onWebSocketError: (event) => {
                console.error('Chat WebSocket error', event);
            },
            onWebSocketClose: (event) => {
                console.error('Chat WebSocket closed', event);
            },
        });

        newChatClient.activate();
        setChatClient(newChatClient);

        return () => {
            if (newClient) {
                newClient.deactivate();
            }
            if (newChatClient) {
                newChatClient.deactivate();
            }
        };
    }, []);



    const handleAcceptChallenge = (challenger) => {
        // Erstelle das Spiel, nachdem die Herausforderung akzeptiert wurde
        createGame(userId, challenger);
        setNotifications(notifications.filter(n => n.senderName !== challenger));
    };

    const createGame = (userA, userB) => {
        if (client && client.connected) {
            client.publish({
                destination: '/app/createGame',
                body: JSON.stringify({ userA, userB }),
            });
            toast.success("Herausforderung gesendet.");
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const handleRejectChallenge = (challenger) => {
        setNotifications(notifications.filter(n => n.senderName !== challenger));
    };

    const handleTimeoutChallenge = (challenger) => {
        setNotifications(notifications.filter(n => n.senderName !== challenger));
    };



    return (
        <WebSocketContext.Provider value={{ client, chatClient, notifications, handleAcceptChallenge, handleRejectChallenge, handleTimeoutChallenge }}>
            {children}
        </WebSocketContext.Provider>
    );
};