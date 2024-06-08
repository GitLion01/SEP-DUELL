import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from 'axios';

const DeckSelection = ({ onSelectDeck }) => {
  const [decks, setDecks] = useState([]);
  const [selectedDeck, setSelectedDeck] = useState(null);
  const [opponentReady, setOpponentReady] = useState(false);
  const [id, setId] = useState(null);
  const [gameId, setGameId] = useState(localStorage.getItem('gameId'));
  const [client, setClient] = useState(null);

  useEffect(() => {
    const loadDecks = async () => {
      if (id) {
        try {
          const decksResponse = await axios.get(`http://localhost:8080/decks/getUserDecks/${id}`);
          setDecks(decksResponse.data);
        } catch (error) {
          console.error('Fehler beim Abrufen der Decks:', error);
        }
      }
    };
    loadDecks();
  }, [id]);

  const newClient = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    onConnect: () => {
      client.subscribe('/topic/deckSelection', (message) => {
        const response = JSON.parse(message.body);
        if (response.playerId !== id) {
          setOpponentReady(true);
          setGameId(response.gameId);
          localStorage.setItem('gameId', response.gameId); // Speichere gameId im LocalStorage
        }
      });
      client.subscribe('/topic/startDuel', (message) => {
        const response = JSON.parse(message.body);
        setGameId(response.gameId);
        localStorage.setItem('gameId', response.gameId); // Speichere gameId im LocalStorage
        onSelectDeck(selectedDeck);
      });
    },
  });

  useEffect(() => {
    newClient.activate();

    return () => {
      newClient.deactivate();
    };
  }, [id, selectedDeck]);

  const handleSelectDeck = (deckId) => {
    setSelectedDeck(deckId);
    client.publish({
      destination: '/app/selectDeck',
      body: JSON.stringify({ 
        userID: id, 
        deckName: deckId, 
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
      <ul>
        {decks.map(deck => (
          <li key={deck.id}>
            {deck.name}
            <button onClick={() => handleSelectDeck(deck.id)}>Select</button>
          </li>
        ))}
      </ul>
      {selectedDeck && <p>Waiting for opponent...</p>}
    </div>
  );
};

export default DeckSelection;
