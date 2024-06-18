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
    const [activeDuel, setActiveDuel] = useState(false);
    const userId = parseInt(localStorage.getItem('id'))

    useEffect(() => {



        const newClient = new Client({
            brokerURL: 'ws://localhost:8080/game-websocket',
            webSocketFactory: () => new SockJS('http://localhost:8080/game-websocket'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket server');
                
                // Überprüfung, ob der Client verbunden ist
                if (newClient.connected) {
                    // Subscribe für Benachrichtigung 
                    newClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
                        const notification = JSON.parse(message.body);
                        if (notification.message === 'challenge') {
                            setNotifications(prev => [...prev, { ...notification, countdown: 30 }]);
                            console.log(notification);
                        } else if (notification.message === 'duelAccepted') {
                            setActiveDuel(true);  // Setze activeDuel auf true
                            setNotifications(prev => [...prev, { ...notification}]);
                            console.log(notifications)
                        }
                    });
        
                    // Subscribe für globale Herausforderung
                    newClient.subscribe(`/user/${userId}/queue/create`, (message) => {
                        const response = JSON.parse(message.body);
                        console.log("Received response:", response);
        
                        if (response.gameId) {
                            localStorage.setItem('gameId', response.gameId);
                            window.dispatchEvent(new CustomEvent('gameCreated', { detail: response.gameId }));
                        }
                    });
                }
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



    const handleAcceptChallenge = (challengerId, challengerName, receiverId) => {
     
        console.log(challengerId, receiverId)
        if (client && client.connected) {
            client.publish({
                destination: '/app/accept.herausforderung',
                headers: {
                    senderId: challengerId.toString(),
                    receiverId: receiverId.toString(),
                },
            });
        setNotifications(notifications.filter(n => n.senderName !== challengerName));
        setActiveDuel(true);
         } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const createGame = (receiverId, senderName) => {  
        if (client && client.connected) {
            client.publish({
                destination: '/app/createGame',
                body: JSON.stringify({ userA: receiverId, userB: senderName }),
            });
            toast.success("Spiel wird gestartet");
            setActiveDuel(false); 
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const handleRejectChallenge = (challengerId) => {
        console.log(challengerId)
        setNotifications(notifications.filter(n => n.senderId !== challengerId));
    };

    const handleTimeoutChallenge = (challengerId) => {
        setNotifications(notifications.filter(n => n.senderId !== challengerId));
    };



    return (
        <WebSocketContext.Provider value={{ client, chatClient, notifications, handleAcceptChallenge, handleRejectChallenge, handleTimeoutChallenge,
            activeDuel, createGame
         }}>
            {children}
        </WebSocketContext.Provider>
    );
};