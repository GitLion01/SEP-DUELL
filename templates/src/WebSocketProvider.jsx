import React, { createContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { toast } from 'react-toastify';
import axios from 'axios';

export const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
    const [client, setClient] = useState(null);
    const [chatClient, setChatClient] = useState(null);
    const [notifications, setNotifications] = useState([]);
    const navigate = useNavigate();
    const [activeDuel, setActiveDuel] = useState(false);
    const userId = parseInt(localStorage.getItem('id'));
    const [game, setGame] = useState(null);
    const [users, setUsers] = useState([]);
    const [connected, setConnected] = useState(false);
    const [botPS, setBotPS] = useState(null);

    useEffect(() => {
        const userId = parseInt(localStorage.getItem('id'));

        const newClient = new Client({
            brokerURL: 'ws://localhost:8080/game-websocket',
            webSocketFactory: () => new SockJS('http://localhost:8080/game-websocket'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket server');

                if (newClient.connected) {
                    newClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
                        const notification = JSON.parse(message.body);
                        console.log('Received notification:', notification);

                        if (notification.message === 'challenge') {
                            setNotifications(prev => [...prev, { ...notification, countdown: 30 }]);
                        } else if (notification.message === 'duelAccepted') {
                            setActiveDuel(true);
                            setNotifications(prev => [...prev, { ...notification }]);
                            window.dispatchEvent(new CustomEvent('duelAccepted'));
                        } else if (notification.message === 'duelRejected') {
                            toast.info('Deine Herausforderung wurde abgelehnt. Du kannst eine neue Herausforderung senden.');
                            setNotifications(prev => prev.filter(n => !(n.senderId === notification.senderId && n.message === 'challenge')));
                            window.dispatchEvent(new CustomEvent('challengeRejected')); 
                        } else if(notification.message === 'schon aktiviert') {
                            setActiveDuel(false);
                            console.log(notification.message);
                        } else if(notification.message === 'turnier') {
                            console.log(notification);
                            setNotifications(prev => [...prev, { ...notification }]);
                        } else if (notification.message === 'turnierDeleted') {
                            toast.info('Das Turnier wurde abgebrochen.');
                            setNotifications(prev => prev.filter(n => n.message !== 'turnier'));
                        } else if (notification.message === 'neueRunde') {
                                toast.info('Es wurde eine neue Runde gestartet');
                                window.dispatchEvent(new CustomEvent('neueRunde'));
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
                    console.log("Received response:", response);
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
    }, [userId]);

    const handleAcceptChallenge = (challengerId, challengerName, receiverId) => {
        console.log(challengerId, receiverId);
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
            updateStatus(receiverId, 'im Duell');
            updateStatus(challengerId, 'im Duell');
            window.dispatchEvent(new CustomEvent('duelAccepted'));
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };


    const updateStatus = (userId, status) => {
        if (client && client.connected) {
            client.publish({
                destination: '/status/status',
                headers: {
                    userId: userId.toString()
                },
                body: status
            });
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const getUserId =  (senderName) => {
        const response =  axios.get(`http://localhost:8080/getId/${senderName}`);
        console.log("Antwort ID", response);
        return response;
    };


    const createGame =  (receiverId, senderName) => {
        const senderID =  getUserId(senderName)
        console.log("hhhhhhhhhhhh",senderID);
        console.log("ReceiverID: ", receiverId, "Sendername: ", senderName);
        if (client && client.connected) {
            client.publish({
                destination: '/app/createGame',
                body: JSON.stringify({userA: receiverId, userB: senderName}),
            });
            toast.success("Spiel wird gestartet");
            setActiveDuel(false);
            window.dispatchEvent(new CustomEvent('duelStarted'));
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const handleRejectChallenge = (receiverId, senderId) => {
        console.log(senderId);
        if (client && client.connected) {
            client.publish({
                destination: '/app/reject.herausforderung',
                headers: {
                    senderId: senderId.toString(),
                    receiverId: receiverId.toString(),
                },
            });
            setNotifications(notifications.filter(n => !(n.senderId === senderId && n.message === 'challenge')));
            window.dispatchEvent(new CustomEvent('challengeRejected')); 
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const handleTimeoutChallenge = (challengerId) => {
        setNotifications(notifications.filter(n => n.senderId !== challengerId));
    };

    const startTournament = (clanId) => {
        if (client && client.connected) {
            client.publish({
                destination: '/app/turnierStart',
                body: JSON.stringify(clanId),
            });
            toast.success("Turnier gestartet");
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const acceptTournament = (userId) => {
        if (client && client.connected) {
            client.publish({
                destination: '/app/turnierAkzeptieren',
                body:JSON.stringify(userId),
            });
            toast.success("Turnier akzeptiert");
            setNotifications(prev => prev.filter(n => n.message !== 'turnier'));
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const sendWinner = (userId) => {
        if (client && client.connected) {
            client.publish({
                destination: '/app/gewinnerSpeichern',
                body: JSON.stringify(userId),
            });
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const rejectTournament = (userId) => {
        if (client && client.connected) {
            client.publish({
                destination: '/app/turnierAblehnen',
                body: JSON.stringify(userId),
            });
            toast.success("Turnier abgelehnt");
            setNotifications(prev => prev.filter(n => n.message !== 'turnier'));
        } else {
            toast.error("WebSocket-Verbindung ist nicht aktiv.");
        }
    };

    const removeNotification = (notificationId) => {
        setNotifications(prev => prev.filter(n => n.id !== notificationId));
    };

    return (
        <WebSocketContext.Provider value={{ client, chatClient, notifications, handleAcceptChallenge, handleRejectChallenge, handleTimeoutChallenge,
            activeDuel, createGame, game, setGame, users, setUsers, connected, startTournament, acceptTournament, rejectTournament, removeNotification, sendWinner, botPS, setBotPS,
         }}>
            {children}
        </WebSocketContext.Provider>
    );
};

export default WebSocketProvider;