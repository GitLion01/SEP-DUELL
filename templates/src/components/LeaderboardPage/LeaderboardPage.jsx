import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import './LeaderboardPage.css';
import BackButton from '../BackButton';
import Notification from './Notification';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const LeaderboardPage = () => {
    const [leaderboard, setLeaderboard] = useState([]);
    const [client, setClient] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [notifications, setNotifications] = useState([]);
    const [countdown, setCountdown] = useState(null);
    const [isChallengeDisabled, setIsChallengeDisabled] = useState(false);
    const [activeDuel, setActiveDuel] = useState(null);
    const [currentUserData, setCurrentUserData] = useState(null); 
    const userId = parseInt(localStorage.getItem('id'), 10);

    

    useEffect(() => { ///////!!!!!!!!!!!!!!!!!!!!!!!!!
        // Abrufen des aktuellen Benutzers aus dem lokalen Speicher
        // Konsolenausgabe zur Überprüfung des abgerufenen Benutzernamens
        console.log('Aktueller UserId:', userId);
        console.log(leaderboard); 


        fetch('http://localhost:8080/leaderboard')
            .then(response => response.json())
            .then(data => { 
                setLeaderboard(data);
                const userData = data.find(user => user.id === userId)
                setCurrentUserData(userData);
                console.log(userData)
            })
        

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
                newClient.subscribe(`/user/${userId}/queue/notifications`, message => {
                    const notification = JSON.parse(message.body);
                    console.log(notification)
                    if (notification.message === 'challenge') {
                       // setCountdown(30); // Start des Countdowns bei 30 Sekunden
                        setIsChallengeDisabled(true); // Herausforderungsbutton deaktivieren
                        setNotifications(prev => [...prev,{ ...notification, countdown:30}]);
                    } else if (notification.type === 'duelAccepted') {
                        setActiveDuel(notification); // Aktives Duell setzen
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
        console.log(leaderboard); 
        return () => {
            if (client) {
                client.deactivate();
            }
        };

        

    }, []);

   /* useEffect(() => {
        if (countdown !== null && countdown > 0) {
            const timer = setInterval(() => {
                setCountdown(prevCountdown => prevCountdown - 1);
            }, 1000);
            return () => clearInterval(timer);
        } else if (countdown === 0) {
            setIsChallengeDisabled(false); // Herausforderungsbutton nach Countdown wieder aktivieren
        }
    }, [countdown]); */

    //zum überprüfen ob ich dieser user bin
    const handleChallenge = async (userId, username) => {
        try {
            const currentUserDeckResponse = await fetch(`http://localhost:8080/decks/getUserDecks/${currentUserData.id}`);
            const currentUserDeckData = await currentUserDeckResponse.json();

            const opponentDeckResponse = await fetch(`http://localhost:8080/decks/getUserDecks/${userId}`);
            const opponentDeckData = await opponentDeckResponse.json();

            if (currentUserDeckData.length === 0) {
                toast.error('Du hast kein Deck!');
                return;
            }

            if (opponentDeckData.length === 0) {
                toast.error(`${username} hat kein Deck!`);
                return;
            }

            if (client) {
                client.publish({
                    destination: '/app/send.herausforderung',
                    headers: { 
                        senderId: currentUserData.id.toString(), 
                        receiverId: userId.toString()
                    },
                    body: JSON.stringify({})
                });
                alert(`Du hast ${username} zu einem Duell herausgefordert!`);
            }
        } catch (error) {
            console.error('Fehler beim Abrufen der Decks:', error);
            toast.error('Fehler beim Abrufen der Decks.');
        }
    };

    const handleAcceptChallenge = (challenger) => {
        if (client) {
            client.publish({
                destination: '/chat/accept-challenge',  //PLATZHALTER
                body: JSON.stringify({ challenger: challenger, challenged: currentUserData.id })
            });
        }
        alert(`Du hast die Herausforderung von ${challenger} akzeptiert!`);
        setNotifications(notifications.filter(n => n.challenger !== challenger));
        setCountdown(null); // Countdown stoppen
        setIsChallengeDisabled(false); // Herausforderungsbutton wieder aktivieren
        setActiveDuel({ challenger, challenged: currentUserData.id }); // Aktives Duell setzen
    };

    const handleRejectChallenge = (challenger) => {
        alert(`Du hast die Herausforderung von ${challenger} abgelehnt!`);
        setNotifications(notifications.filter(n => n.challenger !== challenger));
        setIsChallengeDisabled(false); // Herausforderungsbutton wieder aktivieren
    };

    const handleTimeoutChallenge = (challenger) => {
        toast.error(`Die Herausforderung von ${challenger} ist abgelaufen!`);
        setNotifications(notifications.filter(n => n.senderName !== challenger));
        setIsChallengeDisabled(false); // Herausforderungsbutton wieder aktivieren
    };

    const filteredLeaderboard = leaderboard.filter(user =>
        user.username.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const filteredNotifications = notifications.filter(n => n.receiverId === userId);

    return (
        <div className="leaderboard-page">
            <BackButton />
            <ToastContainer />
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
                                onClick={() => handleChallenge(user.id, user.username)}
                                disabled={user.status !== 'online' || isChallengeDisabled || user.id === currentUserData.id }
                            >
                                Duell herausfordern
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            <div className="notifications">
                {filteredNotifications.map((notification, index) => (
                    <Notification
                        key={index}
                        challenger={notification.senderName}
                        onAccept={handleAcceptChallenge}
                        onReject={handleRejectChallenge}
                        message={notification.message} // Hier das type-Feld übergeben
                        countdown={notification.countdown}
                        onTimeout={handleTimeoutChallenge}
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