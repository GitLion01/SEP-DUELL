import React, {useState, useEffect, useContext} from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { WebSocketContext} from "../../WebSocketProvider";

const DeckSelection = () => {
  const { client } = useContext(WebSocketContext); // Verwende den Kontext
  const [decks, setDecks] = useState([]);
  const [selectedDeck, setSelectedDeck] = useState(null);
  const [id, setId] = useState(null);
  const [gameId, setGameId] = useState('');
  const [game, setGame] = useState(null);
  const navigate = useNavigate(); // Use navigate to redirect

  useEffect(() => {
    const userId = localStorage.getItem('id');
    const gId = localStorage.getItem('gameId');
    console.log(gId);
    if (userId && gId) {
      setId(userId);
      setGameId(gId);
    } else {
      console.error('Keine Benutzer-ID im LocalStorage gefunden.');
    }
  }, []);

  useEffect(() => {
    const loadDecks = async () => {
      if (id) {
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
  }, [id]);

  useEffect(() => {
    if (client && client.connected) {
      const subscription = client.subscribe(`/user/${id}/queue/selectDeck`, (message) => {
        const response = JSON.parse(message.body);
        console.log(response);
        console.log(response.ready);
        if (response.id === gameId && response.ready === true) {
          localStorage.setItem('game', response);
          setGame(response);
          navigate('/duel');
        }
      });
      //return () => subscription.unsubscribe(); // Cleanup function
    }
  }, [client, id, gameId, navigate]);


  const handleSelectDeck = (deckId) => {
    if (client && client.connected) { // Überprüfe, ob client existiert und verbunden ist
      setSelectedDeck(deckId);
      client.publish({
        destination: '/app/selectDeck',
        body: JSON.stringify({
          gameId: gameId,
          deckId: deckId,
          userId: id
        }),
      });
    } else {
      console.error('WebSocket client ist nicht verbunden.');
    }
  };


  const terminateGame = () => {
    client.publish({
      destination: '/app/terminate'
    })
  }

  return (
      <div>
        <h2>Select Your Deck</h2>
        <div className="deck-list">
          {decks.map((deck, index) => (

              <div key={index}
                   className="deck" onClick={() => handleSelectDeck(deck.id)}
                   >
                {deck.name}
              </div>

          ))}
        </div>
        {selectedDeck && <p>Waiting for opponent...</p>}
      </div>
  );
};

export default DeckSelection;
