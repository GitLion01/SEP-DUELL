import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from 'axios';

const DeckSelection = ({ client }) => {
  const [decks, setDecks] = useState([]);
  const [selectedDeck, setSelectedDeck] = useState(null);
  const [opponentReady, setOpponentReady] = useState(false);
  const [id, setId] = useState(null);
  const [gameId, setGameId] = useState('');

  useEffect(() => {
    const userId = localStorage.getItem('id');
    if (userId) {
      setId(userId);
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
      client.subscribe('/all/game', (message) => {
        const response = JSON.parse(message.body);
        if (response.playerId !== id) {
          setOpponentReady(true);
          setGameId(response.gameId);
          localStorage.setItem('gameId', response.gameId);
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

  useEffect(() => {
    if (selectedDeck && opponentReady) {
      client.publish({
        destination: '/app/startDuel',
        body: JSON.stringify({ gameId }),
      });
    }
  }, [selectedDeck, opponentReady, gameId]);

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
