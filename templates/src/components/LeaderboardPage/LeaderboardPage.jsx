import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import './LeaderboardPage.css';
import BackButton from '../BackButton';
import Notification from './Notification';

const LeaderboardPage = () => {
    const [leaderboard, setLeaderboard] = useState([]);
    const [client, setClient] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [notifications, setNotifications] = useState([]);
    const [countdown, setCountdown] = useState(null);
    const [isChallengeDisabled, setIsChallengeDisabled] = useState(false);
    const [activeDuel, setActiveDuel] = useState(null);
    const [currentUser, setCurrentUser] = useState('');

    useEffect(() => { ///////!!!!!!!!!!!!!!!!!!!!!!!!!
        // Abrufen des aktuellen Benutzers aus dem lokalen Speicher
        const username = localStorage.getItem('id');
        setCurrentUser(username);


        // Konsolenausgabe zur Überprüfung des abgerufenen Benutzernamens
        console.log('Aktueller Benutzername:', username);


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
                    setLeaderboard(prev => {
                        const index = prev.findIndex(user => user.id === updatedUser.id);
                        if (index !== -1) {
                            const updatedLeaderboard = [...prev];
                            updatedLeaderboard[index] = updatedUser;
                            return updatedLeaderboard;
                        } else {
                            return [...prev, updatedUser];
                        }
                    });
                });
                newClient.subscribe('/user/queue/notifications', message => {
                    const notification = JSON.parse(message.body);
                    if (notification.type === 'challenge') {
                        setCountdown(30); // Start des Countdowns bei 30 Sekunden
                        setIsChallengeDisabled(true); // Herausforderungsbutton deaktivieren
                    } else if (notification.type === 'duelAccepted') {
                        setActiveDuel(notification); // Aktives Duell setzen
                    }
                    setNotifications(prev => [...prev, notification]);
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

    useEffect(() => {
        if (countdown !== null && countdown > 0) {
            const timer = setInterval(() => {
                setCountdown(prevCountdown => prevCountdown - 1);
            }, 1000);
            return () => clearInterval(timer);
        } else if (countdown === 0) {
            setIsChallengeDisabled(false); // Herausforderungsbutton nach Countdown wieder aktivieren
        }
    }, [countdown]);

    //zum überprüfen ob ich dieser user bin
    const handleChallenge = (username) => {
        if (username === currentUser) {
            alert('Du kannst dich nicht selbst herausfordern!');
            return;
        }

        if (client) {
            client.publish({
                destination: '/app/challenge',  //PLATZHALTER
                body: JSON.stringify({ challenger: currentUser, challenged: username })//!!!!!!!Platzhalter
            });
            alert(`Du hast ${username} zu einem Duell herausgefordert!`);
        }
    };

    const handleAcceptChallenge = (challenger) => {
        if (client) {
            client.publish({
                destination: '/app/accept-challenge',  //PLATZHALTER
                body: JSON.stringify({ challenger: challenger, challenged: currentUser })
            });
        }
        alert(`Du hast die Herausforderung von ${challenger} akzeptiert!`);
        setNotifications(notifications.filter(n => n.challenger !== challenger));
        setCountdown(null); // Countdown stoppen
        setIsChallengeDisabled(false); // Herausforderungsbutton wieder aktivieren
        setActiveDuel({ challenger, challenged: currentUser }); // Aktives Duell setzen
    };

    const handleRejectChallenge = (challenger) => {
        alert(`Du hast die Herausforderung von ${challenger} abgelehnt!`);
        setNotifications(notifications.filter(n => n.challenger !== challenger));
        setCountdown(null); // Countdown stoppen
        setIsChallengeDisabled(false); // Herausforderungsbutton wieder aktivieren
    };

    const filteredLeaderboard = leaderboard.filter(user =>
        user.username.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="leaderboard-page">
            <BackButton />
            <input
                type="text"
                placeholder="Nach Benutzername suchen..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="search-input"
            />
            <table className="leaderboard-table">
                <thead>
                <tr>
                    <th>Rang</th>
                    <th>Benutzername</th>
                    <th>Punkte</th>
                    <th>Status</th>
                    <th>Aktion</th>
                </tr>
                </thead>
                <tbody>
                {filteredLeaderboard.map((user, index) => (
                    <tr key={user.id}>
                        <td>{index + 1}</td>
                        <td>{user.username}</td>
                        <td>{user.leaderboardPoints}</td>
                        <td className={`status ${user.status}`}>{user.status}</td>
                        <td>
                            <button
                                className="challenge-button"
                                onClick={() => handleChallenge(user.username)}
                                disabled={user.status !== 'online' || isChallengeDisabled || user.username === currentUser}
                            >
                                Duell herausfordern
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            <div className="notifications">
                {notifications.map((notification, index) => (
                    <Notification
                        key={index}
                        challenger={notification.challenger}
                        onAccept={handleAcceptChallenge}
                        onReject={handleRejectChallenge}
                        type={notification.type} // Hier das type-Feld übergeben
                    />
                ))}
            </div>
            {countdown !== null && countdown > 0 && (
                <div className="countdown">
                    <p>Countdown: {countdown} Sekunden</p>
                </div>
            )}
            {activeDuel && (
                <div className="active-duel">
                    <a href="/duel">
                        <button className="active-duel-button">Aktives Duell</button>
                    </a>
                </div>
            )}
        </div>
    );
};

export default LeaderboardPage;
