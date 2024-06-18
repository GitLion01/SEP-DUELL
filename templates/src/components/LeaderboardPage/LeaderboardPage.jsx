import React, { useContext, useState, useEffect } from 'react';
import { WebSocketContext } from '../../WebSocketProvider';
import './LeaderboardPage.css';
import BackButton from '../BackButton';
import Notification from './Notification';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const LeaderboardPage = () => {
    const { client, notifications, handleAcceptChallenge, handleRejectChallenge, handleTimeoutChallenge } = useContext(WebSocketContext);
    const [leaderboard, setLeaderboard] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [isChallengeDisabled, setIsChallengeDisabled] = useState(false);
    const [activeDuel, setActiveDuel] = useState(null);
    const [currentUserData, setCurrentUserData] = useState(null); 
    const userId = parseInt(localStorage.getItem('id'), 10);
    const [countdown, setCountdown ] = useState(null);

    useEffect(() => {
        console.log('Aktueller UserId:', userId);

        fetch('http://localhost:8080/leaderboard')
            .then(response => response.json())
            .then(data => { 
                setLeaderboard(data);
                const userData = data.find(user => user.id === userId);
                setCurrentUserData(userData);
                console.log(userData);
            });

            if (client && client.connected) {
                const leaderboardSubscription = client.subscribe('/topic/leaderboard', message => {
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
    
                return () => {
                    // Deaktiviere die Abonnements bei der Bereinigung
                    if (leaderboardSubscription) {
                        leaderboardSubscription.unsubscribe();
                    }
                };
            }
        }, [userId, client]);

    const handleChallenge = async (userId, username) => {
        try {
            const currentUserDeckResponse = await fetch(`http://localhost:8080/decks/getUserDecks/${currentUserData.id}`);
            const currentUserDeckData = await currentUserDeckResponse.json();
            console.log(currentUserDeckData)
            const opponentDeckResponse = await fetch(`http://localhost:8080/decks/getUserDecks/${userId}`);
            const opponentDeckData = await opponentDeckResponse.json();

            const currentUserDeckHasEnoughCards = await Promise.all(
                currentUserDeckData.map(async (deck) => {
                    const deckCardsResponse = await fetch(`http://localhost:8080/cards/${deck.name}/${currentUserData.id}`);
                    const deckCards = await deckCardsResponse.json();
                    return deckCards.length >= 5;
                })
            );

            const opponentDeckHasEnoughCards = await Promise.all(
                opponentDeckData.map(async (deck) => {
                    const deckCardsResponse = await fetch(`http://localhost:8080/cards/${deck.name}/${userId}`);
                    const deckCards = await deckCardsResponse.json();
                    return deckCards.length >= 5;
                })
            );

            if (!currentUserDeckHasEnoughCards.includes(true)) {
                toast.error('Keines deiner Decks hat genug Karten (mindestens 5)!');
                return;
            }

            if (!opponentDeckHasEnoughCards.includes(true)) {
                toast.error(`${username} hat kein Deck mit genug Karten (mindestens 5)!`);
                return;
            }




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
                toast.success(`Du hast ${username} zu einem Duell herausgefordert!`);
                setIsChallengeDisabled(true); // Herausforderungsbutton deaktivieren
                setCountdown(30); // Start des Countdowns bei 30 Sekunden
            }
        } catch (error) {
            console.error('Fehler beim Abrufen der Decks:', error);
            toast.error('Fehler beim Abrufen der Decks.');
        }
    };

    const filteredLeaderboard = leaderboard.filter(user =>
        user.username.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const filteredNotifications = notifications.filter(n => n.receiverId === userId);
    
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
                                    disabled={user.status !== 'online' || isChallengeDisabled || user.id === currentUserData.id}
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
                        senderId={notification.senderId}
                        senderName={notification.senderName}
                        receiverId={notification.receiverId}
                        onAccept={handleAcceptChallenge}
                        onReject={handleRejectChallenge}
                        message={notification.message}
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
