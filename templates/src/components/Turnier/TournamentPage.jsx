import React, { useEffect, useState, useContext } from 'react';
import { toast } from 'react-toastify';
import { WebSocketContext } from '../../WebSocketProvider';
import './TournamentPage.css';
import BackButton from '../BackButton';

const TournamentPage = () => {
    const [matches, setMatches] = useState([]);
    const [clanId, setClanId] = useState(null);
    const userId = parseInt(localStorage.getItem('id'));
    const userName = localStorage.getItem('username'); // Assuming username is stored in localStorage
    const { createGame } = useContext(WebSocketContext);

    useEffect(() => {
        fetchClanId();
    }, [userId]);

    useEffect(() => {
        if (clanId) {
            fetchTurnierMatches();
        }
    }, [clanId]);

    const fetchTurnierMatches = async () => {
        try {
            const response = await fetch(`http://localhost:8080/getTurnier?clanId=${clanId}`);
            if (response.ok) {
                const data = await response.json();
                console.log('Turnier Matches:', data);
                setMatches(data);
            } else {
                throw new Error('Network response was not ok');
            }
        } catch (error) {
            console.error('Error fetching turnier matches:', error);
            toast.error('Fehler beim Abrufen der Turnierdaten');
        }
    };

    const fetchClanId = async () => {
        try {
            const response = await fetch(`http://localhost:8080/getClanId?userId=${userId}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setClanId(data);
            localStorage.setItem('clanId', data);
        } catch (error) {
            console.error('There was a problem with the fetch operation:', error);
        }
    };

    const handleGameCreation = (receiverId, opponentName) => {
        createGame(userId, opponentName); // Pass userId and opponent's username
    };

    return (
        <div className="container">
        <BackButton/>
            <h1>Turnier Seite</h1>
            {matches.length === 0 ? (
                <p>Keine Matches vorhanden.</p>
            ) : (
                <ul>
                    {matches.map((match, index) => (
                        <li key={index} className="match">
                            <span>{match.userName1}</span>
                            <span> vs </span>
                            <span>{match.userName2}</span>
                            {(match.player1 === userId || match.player2 === userId) && (
                                <button
                                    className="start-game-button"
                                    onClick={() => handleGameCreation(
                                        userId,
                                        match.player1 === userId ? match.userName2 : match.userName1
                                    )}
                                >
                                    Spiel starten
                                </button>
                            )}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default TournamentPage;
