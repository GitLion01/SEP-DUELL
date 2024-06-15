import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const DeckSelection = ({ client }) => {
  const [decks, setDecks] = useState([]);
  const [selectedDeck, setSelectedDeck] = useState(null);
  const [id, setId] = useState(null);
  const [gameId, setGameId] = useState('');
  const navigate = useNavigate(); // Use navigate to redirect

  useEffect(() => {
    const userId = localStorage.getItem('id');
    const gId = localStorage.getItem('gameId');
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
    if (client) {
      client.subscribe(`/user/${id}/queue/game`, (message) => {
        const response = JSON.parse(message.body);
        // Überprüfe, ob beide Spieler bereit sind
        if (response.game.id === gameId && response.game.ready) {
          navigate('/duel'); // Redirect to duel page
        }
      });
    }
  }, [client, id]);

  const handleSelectDeck = (deckId) => {
    setSelectedDeck(deckId);
    client.publish({
      destination: '/app/selectDeck',
      body: JSON.stringify({ 
        deckId: deckId,
        gameID: gameId
      }),
    });
  };

  const terminateGame = () => {
    client.publish({
      destination: '/app/terminate'
    })
  }

  useEffect(() => {
    if (selectedDeck) {
      client.publish({
        destination: '/app/startDuel',
        body: JSON.stringify({ gameId }),
      });
    }
  }, [selectedDeck, gameId]);

  return (
      <div>
        <h2>Select Your Deck</h2>
        <div className="deck-list">
          {decks.map((deck, index) => (

              <div key={index}
                   className="deck"
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
