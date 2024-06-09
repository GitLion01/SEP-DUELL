import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

function DuelC({ client }) {
    const [username, setUsername] = useState('');
    const [isClientConnected, setIsClientConnected] = useState(false);
    const navigate = useNavigate();
    const userID = localStorage.getItem('id');

    const handleChallenge = () => {
        if (username && isClientConnected) {
            // Nachricht an den WebSocket-Server senden
            client.publish({
                destination: '/app/createGame',
                body: JSON.stringify({ userA: userID, userB: username }),
            });
            toast.success("Herausforderung gesendet.");
        } else {
            toast.error("Bitte geben Sie einen gültigen Benutzernamen ein und stellen Sie sicher, dass die Verbindung aktiv ist.");
        }
    };

    useEffect(() => {
        const checkConnection = () => {
            if (client && client.connected) {
                setIsClientConnected(true);
                console.log("Client verbunden");
                const subscription = client.subscribe('/all/create', (message) => {
                    const response = JSON.parse(message.body);
                    if (response.gameId) {
                        localStorage.setItem('gameId', response.gameId);
                        navigate('/deck-selection'); // Weiterleitung zur Deckauswahl
                    }
                });
                // Cleanup function
                return () => subscription.unsubscribe();
            } else {
                setTimeout(checkConnection, 100); // Erneut nach 100ms überprüfen
            }
        };

        checkConnection(); // Verbindung überprüfen
    }, [client, navigate]);

    return (
        <div>
            <h2>Spieler herausfordern</h2>
            <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Benutzername eingeben"
            />
            <button onClick={handleChallenge}>Herausfordern</button>
        </div>
    );


}

export default DuelC;