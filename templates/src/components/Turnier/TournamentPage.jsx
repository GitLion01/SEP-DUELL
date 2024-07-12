import React, { useEffect, useState, useContext } from 'react';
import { toast } from 'react-toastify';
import { WebSocketContext } from '../../WebSocketProvider';
import './TournamentPage.css';
import BackButton from '../BackButton';

const TournamentPage = () => {
    const [userMatches, setUserMatches] = useState([]);
    const [otherMatches, setOtherMatches] = useState([]);
    const [clanId, setClanId] = useState(null);
    const [winners, setWinners] = useState([]);
    const [showBetModal, setShowBetModal] = useState(false);
    const [currentTurnierId, setCurrentTurnierId] = useState(null);
    const [clanMembers, setClanMembers] = useState([]);
    const [selectedMember, setSelectedMember] = useState(null);
    const userId = parseInt(localStorage.getItem('id'));
    const { createGame, sendWinner } = useContext(WebSocketContext);

    useEffect(() => {
        fetchClanId();
    }, [userId]);

    useEffect(() => {
        if (clanId) {
            fetchTurnierId();
            fetchClanMembers();
        }
    }, [clanId]);

    useEffect(() => {
        if (currentTurnierId) {
            checkIfUserCanBet();
        }
    }, [currentTurnierId]);

    useEffect(() => {
        if (clanId && !showBetModal) {
            fetchTurnierMatches();
            fetchWinners();
        }
    }, [clanId, showBetModal]);

    useEffect(() => {
        const handleNeueRunde = () => {
            fetchTurnierMatches();
        };
        window.addEventListener('neueRunde', handleNeueRunde);
        return () => {
            window.removeEventListener('neueRunde', handleNeueRunde);
        };
    }, []);

    const checkIfUserCanBet = async () => {
        const turnierId = localStorage.getItem('turnierId');
        console.log('Stored turnierId:', turnierId);
        console.log('Current turnierId:', currentTurnierId);
        if (turnierId === null || turnierId !== currentTurnierId.toString()) {
            setShowBetModal(true);
        } else {
            setShowBetModal(false);
        }
    };

    const fetchTurnierId = async () => {
        if (clanId) {
            try {
                const response = await fetch(`http://localhost:8080/getTurnierId?clanId=${clanId}`);
                const id = await response.json();
                setCurrentTurnierId(id);
            } catch (error) {
                console.error('Error fetching turnierId:', error);
            }
        }
    };

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
            const response = await fetch(`http://localhost:8080/getGewinner?clanId=${clanId}`);
            if (response.ok) {
                const data = await response.json();
                console.log('Winners:', data);
                setWinners(data);
            } else {
                throw new Error('Network response was not ok');
            }
        } catch (error) {
            console.error('Error fetching winners:', error);
            toast.error('Fehler beim Abrufen der Gewinnerdaten');
        }
    };

    const fetchClanMembers = async () => {
        try {
            const response = await fetch(`http://localhost:8080/getClanMitglieder?clanId=${clanId}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setClanMembers(data);
            console.log(data);
        } catch (error) {
            console.error('There was a problem with the fetch operation:', error);
            toast.error('Es gab einen Fehler beim Aufruf der Mitglieder');
        }
    };

    const sortMatches = (matches) => {
        const userMatchesArray = [];
        const otherMatchesArray = [];

        matches.forEach((match) => {
            if (match.player2 === null) {
                sendWinner(match.player1);
                if (match.player1 === userId) {
                    userMatchesArray.push(match);
                } else {
                    otherMatchesArray.push(match);
                }
            } else if (match.player1 === userId || match.player2 === userId) {
                userMatchesArray.push(match);
            } else {
                otherMatchesArray.push(match);
            }
        });

        setUserMatches(userMatchesArray);
        setOtherMatches(otherMatchesArray);
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
            await Promise.all([
                setUserInTurnier(userId),
                setUserInTurnier(opponentId)
            ]);

            createGame(userId, opponentName);
        } catch (error) {
            console.error('Error during game creation:', error);
        }
    };

    //Damit Spieler nicht wieder gegeneinander spielen können
    const canStartGame = (player1, player2) => {
        return !winners.includes(player1) && !winners.includes(player2);
    };

    const handleBetSubmission = async () => {
        if (selectedMember) {
            try {
                const response = await fetch(`http://localhost:8080/placeBet`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        bettorId: userId,
                        betOnId: selectedMember.id,
                    }),
                });

                if (response.ok) {
                    localStorage.setItem('turnierId', currentTurnierId);
                    setShowBetModal(false);
                    toast.success('Wette erfolgreich abgegeben');
                } else {
                    const errorMessage = await response.text();
                    toast.error(`Fehler beim Abgeben der Wette: ${errorMessage}`);
                }
            } catch (error) {
                console.error('Error placing bet:', error);
                toast.error('Fehler beim Abgeben der Wette');
            }
        } else {
            toast.error('Bitte wählen Sie ein Clanmitglied aus');
        }
    };

    const handleSkipBetting = () => {
        localStorage.setItem('turnierId', currentTurnierId);
        setShowBetModal(false);
        toast.info('Wetten übersprungen.');
    };

    return (
        <div className="container">
            <BackButton />
            <h1 className="tournament-title">Turnier Seite</h1>
            {showBetModal ? (
                <div className="bet-modal">
                    <h2>Wette abgeben</h2>
                    <select onChange={(e) => setSelectedMember(JSON.parse(e.target.value))}>
                        <option value="">Wählen Sie ein Clanmitglied aus</option>
                        {clanMembers.map(member => (
                            <option key={member.id} value={JSON.stringify(member)}>
                                {member.username}
                            </option>
                        ))}
                    </select>
                    <button onClick={handleBetSubmission}>Wette abgeben</button>
                    <button onClick={handleSkipBetting}>Wetten überspringen</button>
                </div>
            ) : (
                <>
                    {userMatches.length === 0 && otherMatches.length === 0 ? (
                        <p className="no-matches">Keine Matches vorhanden.</p>
                    ) : (
                        <div className="matches-container">
                            <h2 className="section-title">Meine Matches</h2>
                            <ul className="matches-list">
                                {userMatches.map((match, index) => (
                                    <li key={index} className="match">
                                        <span className="player-name">{match.userName1}</span>
                                        <span className="vs"> vs </span>
                                        <span className="player-name">{match.userName2 || "Freilos"}</span>
                                        {match.player2 !== null && canStartGame(match.player1, match.player2) && (
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
                            <h2 className="section-title">Andere Matches</h2>
                            <ul className="matches-list">
                                {otherMatches.map((match, index) => (
                                    <li key={index} className="match">
                                        <span className="player-name">{match.userName1}</span>
                                        <span className="vs"> vs </span>
                                        <span className="player-name">{match.userName2 || "Freilos"}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default TournamentPage;