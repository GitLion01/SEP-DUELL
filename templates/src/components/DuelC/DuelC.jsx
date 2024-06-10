import React, {useEffect, useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { WebSocketContext} from "../../WebSocketProvider";// Importiere den WebSocketContext


function DuelC() {
    const [username, setUsername] = useState('');
    const { client } = useContext(WebSocketContext);
    const userID = localStorage.getItem('id');

    const handleChallenge = () => {
        if (username && client && client.connected) {
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