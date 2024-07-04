// TurnierPage.jsx
import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

const TournamentPage = () => {
    const [matches, setMatches] = useState([]);
    const clanId = localStorage.getItem('clanId'); // Assuming clanId is stored in localStorage

    useEffect(() => {
        const fetchTurnierMatches = async () => {
            try {
                const response = await fetch(`http://localhost:8080/getTurnier?clanId=${clanId}`);
                if (response.ok) {
                    const data = await response.json();
                    console.log('Turnier Matches:', data); // Log to see what is returned
                    setMatches(data);
                } else {
                    throw new Error('Network response was not ok');
                }
            } catch (error) {
                console.error('Error fetching turnier matches:', error);
                toast.error('Fehler beim Abrufen der Turnierdaten');
            }
        };

        fetchTurnierMatches();
    }, [clanId]);

    return (
        <div>
            <h1>Turnier Seite</h1>
            {matches.length === 0 ? (
                <p>Keine Matches vorhanden.</p>
            ) : (
                <ul>
                    {matches.map((match, index) => (
                        <li key={index}>
                            {match.player1} vs {match.player2}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default TournamentPage;
