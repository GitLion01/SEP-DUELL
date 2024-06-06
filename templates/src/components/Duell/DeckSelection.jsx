import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from 'axios';

const DeckSelection = ({ onSelectDeck }) => {
  const [decks, setDecks] = useState([]);
  const [selectedDeck, setSelectedDeck] = useState(null);
  const [opponentReady, setOpponentReady] = useState(false);
  const [id, setId] = useState(null);
  const [gameId, setGameId] = useState(null);

  useEffect(() => {
    const loadId = async () => {
        const loadedId = localStorage.getItem('id');
        if (loadedId) {
            console.log('Geladene ID aus LocalStorage:', loadedId);  // Debugging
            setId(loadedId);
        } else {
            console.error('Keine userID im LocalStorage gefunden');
        }
    };
    loadId();
}, []);

    useEffect(() => {
        const loadDecks = async () => {
            if (id !== null) {

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

  const client = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    connectHeaders: {
      login: 'guest',
      passcode: 'guest',
    },
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    onConnect: () => {
      client.subscribe('/topic/deckSelection', (message) => {
        const response = JSON.parse(message.body);
        if (response.playerId !== id) {
          setOpponentReady(true);
        }
      });
      client.subscribe('/topic/startDuel', () => {
        onSelectDeck(selectedDeck);
      });
    },
  });

  useEffect(() => {
    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  const handleSelectDeck = (deckId) => {
    setSelectedDeck(deckId);
    client.publish({
      destination: '/app/selectDeck',
      body: JSON.stringify({ playerId: 'yourPlayerId', deckId }),
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
