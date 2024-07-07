import React, { useEffect, useState, useContext } from 'react';
import { toast } from 'react-toastify';
import { WebSocketContext } from '../../WebSocketProvider';
import './TournamentPage.css';
import BackButton from '../BackButton';

const TournamentPage = () => {
    const [userMatches, setUserMatches] = useState([]);
    const [otherMatches, setOtherMatches] = useState([]);
    const [clanId, setClanId] = useState(null);
    const userId = parseInt(localStorage.getItem('id'));
    const { createGame, sendWinner } = useContext(WebSocketContext);
    const [winners, setWinners] = useState ([])

    useEffect(() => {
        fetchClanId();
    }, [userId]);

    useEffect(() => {
        if (clanId) {
            fetchTurnierMatches();
            fetchWinners(); 
        }
    }, [clanId]);

    
    useEffect(() => {
        const handleNeueRunde = () => {
            fetchTurnierMatches();
        };
        window.addEventListener('neueRunde', handleNeueRunde);
        return () => {
            window.removeEventListener('neueRunde', handleNeueRunde);
        };
    }, []);

    const fetchTurnierMatches = async () => {
        try {
            const response = await fetch(`http://localhost:8080/getTurnier?clanId=${clanId}`);
            if (response.ok) {
                const data = await response.json();
                console.log('Turnier Matches:', data);
                sortMatches(data);
            } else {
                throw new Error('Network response was not ok');
            }
        } catch (error) {
            console.error('Error fetching turnier matches:', error);
            toast.error('Fehler beim Abrufen der Turnierdaten');
        }
    };

    const fetchWinners = async () => {
        try {
            const response = await fetch(`http://localhost:8080/getWinner?clanId=${clanId}`);
            if (response.ok) {
                const data = await response.json();
                console.log('Winners:', data);
                setWinners(data);
                } else {
                    throw new Error('Network response was not ok');
                    }
        }
        catch (error) {
            console.error('Error fetching winners:', error);
        }

}

    const sortMatches = (matches) => {
        const userMatchesArray = []; 
        const otherMatchesArray = []; 

        matches.forEach((match) => {
            if (match.player2 === null) {
                sendWinner(match.player1)
            }
           else  if (match.player1 === userId || match.player2 === userId) {
                userMatchesArray.push(match);
            } else {
                otherMatchesArray.push(match);
            }
        });

        setUserMatches(userMatchesArray); 
        setOtherMatches(otherMatchesArray);
    }

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

    const setUserInTurnier = async (userId) => {
        try {
            await fetch(`http://localhost:8080/setUserInTurnier?userId=${userId}`, {
                method: 'PUT'
            });
        } catch (error) {
            console.error('Error setting user in tournament:', error);
            toast.error('Fehler beim Hinzufügen des Benutzers zum Turnier');
        }
    };

    const handleGameCreation = async (opponentId, opponentName) => {
        try {
            // Set both users in the tournament
            await Promise.all([
                setUserInTurnier(userId),
                setUserInTurnier(opponentId)
            ]);

            // Create the game
            createGame(userId, opponentName);
        } catch (error) {
            console.error('Error during game creation:', error);
        }
    };

    const canStartGame = (player1, player2) => {
        return !winners.includes(player1) && !winners.includes(player2);
    };

    return (
        <div className="container">
            <BackButton />
            <h1>Turnier Seite</h1>
            {userMatches.length === 0 && otherMatches.length === 0 ? (
                <p>Keine Matches vorhanden.</p>
            ) : (
                <div>
                    <h2>Meine Matches</h2>
                    <ul>
                        {userMatches.map((match, index) => (
                            <li key={index} className="match">
                                <span>{match.userName1}</span>
                                <span> vs </span>
                                <span>{match.userName2 || "Freilos"}</span> {/* Anzeigen "Freilos" wenn kein Gegner vorhanden */}
                                {match.player2 !== null && canStartGame(match.player1, match.player2)(
                                    <button
                                        className="start-game-button"
                                        onClick={() => handleGameCreation(
                                            match.player1 === userId ? match.player2 : match.player1,
                                            match.player1 === userId ? match.userName2 : match.userName1
                                        )}
                                    >
                                        Spiel starten
                                    </button>
                                )}
                            </li>
                        ))}
                    </ul>
                    <h2>Andere Matches</h2>
                    <ul>
                        {otherMatches.map((match, index) => (
                            <li key={index} className="match">
                                <span>{match.userName1}</span>
                                <span> vs </span>
                                <span>{match.userName2 || "Freilos"}</span>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default TournamentPage;
