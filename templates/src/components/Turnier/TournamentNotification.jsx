import React, { useContext } from 'react';
import '../LeaderboardPage/Notification.css';
import { toast } from 'react-toastify';
import { WebSocketContext } from '../../WebSocketProvider';


const TournamentNotification = () => {
    const {acceptTournament, rejectTournament} = useContext(WebSocketContext);
    const userId = localStorage.getItem('id')
    const handleAccept = async () => {
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
        } catch (error) {
            console.error('Fehler beim Abrufen der Decks:', error);
            toast.error('Fehler beim Abrufen der Decks.');
        }
    };

    const handleReject = () => {
        rejectTournament(userId);
    };

    return (
        <div className="notification">
            <p>Dein Clan hat dich zu einem Turnier eingeladen!</p>
            <button onClick={handleAccept}>Akzeptieren</button>
            <button onClick={handleReject}>Ablehnen</button>
        </div>
    );
};

export default TournamentNotification;
