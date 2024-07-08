import React, {useState, useEffect, useContext} from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { WebSocketContext} from "../../WebSocketProvider";
import './BotDeckSelect.css';
import BackButton from '../BackButton';


const BotDeckSelect = () => {
    const { client, setGame, users, setUsers, setBotPS, connected } = useContext(WebSocketContext); // Verwende den Kontext
    const [decks, setDecks] = useState([]);
    const [selectedDeck, setSelectedDeck] = useState(null);
    const [id, setId] = useState(null);
    const [isChecked, setIsChecked] = useState(false); // Zustand für Checkbox
    const navigate = useNavigate(); // Use navigate to redirect

    useEffect(() => {
        const userId = localStorage.getItem('id');
        if (userId && id === null) {
            setId(userId);
        } else {
            console.error('Keine Benutzer-ID im LocalStorage gefunden.');
        }
    }, []);


    useEffect(() => {
        const loadDecks = async () => {
            if (id && connected) {
                try {
                    const decksResponse = await axios.get(`http://localhost:8080/decks/getUserDecks/${id}`);
                    console.log('Received decks:', decksResponse.data);
                    setDecks(decksResponse.data);
                } catch (error) {
                    console.error('Fehler beim Abrufen der Decks:', error);
                }
            }
        };
        loadDecks();
    }, [id, connected]);


    useEffect(() => {
        if (client && connected) {
            const subscription = client.subscribe(`/user/${id}/queue/createBotDuel`, (message) => {
                const response = JSON.parse(message.body);
                console.log("Spiel wurde erstellt: ", response);

                setGame(response[0]);
                setUsers(response[1]);
                setBotPS(response[2]);

                // Speichern des Spiels und der Benutzer im Webspeicher TODO zu testen!!!
                sessionStorage.setItem('game', response[0]);
                sessionStorage.setItem('users', response[1]);
                sessionStorage.setItem('botPS', response[2]);

                localStorage.setItem('gameId', response[0].id);


                navigate('/botduel');


            });

            return () => subscription.unsubscribe(); // Cleanup function
        }
    }, [client, id, navigate, connected]);

    const handleSelectDeck = (deckId) => {
        if (client && client.connected && selectedDeck === null) { // Überprüfe, ob client existiert und verbunden ist
            console.log("GESENDETE DECKID: ", deckId);
            setSelectedDeck(deckId);
            client.publish({
                destination: '/app/createBotGame',
                body: JSON.stringify({
                    deckId: deckId,
                    userId: id,
                    streamed: isChecked
                }),
            });
        } else {
            console.error('WebSocket client ist nicht verbunden.');
        }
    };

    //TODO Checkbox für das aktivieren vom streamen
    const handleRadioChanged = (event) => {

        const newIsChecked = event.target.checked;
        setIsChecked(newIsChecked); // Setzt den neuen Zustand der Checkbox
        console.log("is Checked: ", isChecked);

    }

    return (
        <div>
            <BackButton /> {/* Komponente für einen Zurück-Button */}
            <h2 className={"u1"}>Wähle ein Deck um das Bot-Duell zu starten</h2>
            <div className="deck-list">
                {decks.map((deck) => (

                    <div key={deck.id}
                         className="deck" onClick={() => handleSelectDeck(deck.id)}
                    >
                        {deck.name}
                    </div>

                ))}
            </div>

            <div className="form-check form-radio">
                <input
                    className="form-check-input"
                    type="radio"
                    id="flexRadioCheckDefault"
                    onChange={handleRadioChanged}
                    checked={isChecked}
                />
                <label className="form-check-label" htmlFor="flexRadioCheckDefault">
                    Stream
                </label>
            </div>
            {selectedDeck && <p className={"p1"}>Waiting for Duel to start...</p>}
        </div>
    );


}

export default BotDeckSelect;