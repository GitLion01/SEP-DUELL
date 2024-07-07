import React, { useContext } from 'react';
import { WebSocketContext } from '../../WebSocketProvider';
import TournamentNotification from './TournamentNotification';
import '../LeaderboardPage/GlobalNotification.css';
import { toast } from 'react-toastify';

const GlobalTournamentNotification = () => {
    const { notifications, acceptTournament, rejectTournament, removeNotification } = useContext(WebSocketContext);
    const userId = localStorage.getItem('id');
    const userName = localStorage.getItem('username');

    const handleAccept = async (notificationId) => {
        try {
            const userDeckResponse = await fetch(`http://localhost:8080/decks/getUserDecks/${userId}`);
            const userDeckData = await userDeckResponse.json();

            const userDeckHasEnoughCards = await Promise.all(
                userDeckData.map(async (deck) => {
                    const deckCardsResponse = await fetch(`http://localhost:8080/decks/cards/${deck.name}/${userId}`);
                    const deckCards = await deckCardsResponse.json();
                    return deckCards.length >= 5;
                })
            );

            if (!userDeckHasEnoughCards.includes(true)) {
                toast.error('Keines deiner Decks hat genug Karten (mindestens 5)!');
                return;
            }

            if (userDeckData.length === 0) {
                toast.error('Du hast kein Deck!');
                return;
            }

            acceptTournament(userId);
            removeNotification(notificationId);
        } catch (error) {
            console.error('Fehler beim Abrufen der Decks:', error);
            toast.error('Fehler beim Abrufen der Decks.');
        }
    };

    const handleReject = (notificationId) => {
        rejectTournament(userId);
        removeNotification(notificationId);
    };

    return (
        <div className="global-notification">
            {notifications
                .filter(notification => notification.message === 'turnier')
                .map((notification, index) => (
                    <TournamentNotification
                        key={index}
                        notificationId={notification.id}
                        onAccept={() => handleAccept(notification.id)}
                        onReject={() => handleReject(notification.id)}
                    />
                ))}
        </div>
    );
};

export default GlobalTournamentNotification;
