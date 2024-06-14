// src/components/LeaderboardPage/LeaderboardPage.jsx
import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import './LeaderboardPage.css';

const LeaderboardPage = () => {
    const [leaderboard, setLeaderboard] = useState([]);
    const [client, setClient] = useState(null);

    useEffect(() => {
        fetch('http://localhost:8080/leaderboard')
            .then(response => response.json())
            .then(data => setLeaderboard(data));

        const newClient = new Client({
            brokerURL: 'ws://localhost:8080/game-websocket',
            webSocketFactory: () => new SockJS('http://localhost:8080/game-websocket'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket server');
                newClient.subscribe('/topic/leaderboard', message => {
                    const updatedUser = JSON.parse(message.body);
                    setLeaderboard(prev => prev.map(user => user.id === updatedUser.id ? updatedUser : user));
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
            if (client) {
                client.deactivate();
            }
        };
    }, []);

    return (
        <div className="leaderboard">
            <h1>Leaderboard</h1>
            <table className="leaderboard-table">
                <thead>
                <tr>
                    <th>Benutzername</th>
                    <th>Punkte</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                {leaderboard.map(user => (
                    <tr key={user.id}>
                        <td>{user.username}</td>
                        <td>{user.leaderboardPoints}</td>
                        <td className={`status ${user.status}`}>{user.status}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default LeaderboardPage;
