import React, {useState, useEffect, useContext} from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { WebSocketContext} from "../../WebSocketProvider";
import './DeckSelection.css'

const  DeckSelection = () => {
  const { client, setGame, users, setUsers, connected } = useContext(WebSocketContext); // Verwende den Kontext
  const [decks, setDecks] = useState([]);
  const [selectedDeck, setSelectedDeck] = useState(null);
  const [id, setId] = useState(null);
  const [gameId, setGameId] = useState('');
  const [isChecked, setIsChecked] = useState(false); // Zustand für Checkbox
  const navigate = useNavigate(); // Use navigate to redirect

  useEffect(() => {
    const userId = localStorage.getItem('id');
    const gId = localStorage.getItem('gameId');
    console.log(gId);
    if (userId && gId && id === null) {
      setId(userId);
      setGameId(gId);
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
      const subscription = client.subscribe(`/user/${id}/queue/selectDeck`, (message) => {
        const response = JSON.parse(message.body);
        console.log("users after deckSelect: ", response[1]);
        /*
          const user1 = users[0].deck = response[0];
          const user2 = users[1].deck = response[1];
          setUsers([user1, user2]);

         */

        setGame(response[0]);
        setUsers(response[1]);
        sessionStorage.setItem('game', JSON.stringify(response[0]));
        sessionStorage.setItem('users', JSON.stringify(response[1]));


        console.log('Users in game: ', response[1]);
        console.log('response from server: ', response);
        console.log('users saved in State: ', response[1]);
        console.log('game saved in State: ', response[0]);

        if (response[0].ready === true) {
          navigate('/duel');
        }
      });

      return () => subscription.unsubscribe(); // Cleanup function
    }
  }, [client, id, gameId, navigate, connected]);

  // Überwachung der Statusänderungen von game und users

  const handleSelectDeck = (deckId) => {
    if (client && client.connected && selectedDeck===null) { // Überprüfe, ob client existiert und verbunden ist
      console.log("GESENDETE DECKID: ", deckId);
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

  //TODO Checkbox für das aktivieren vom streamen
  const handleRadioChanged = (event) => {

    const newIsChecked = event.target.checked;
    setIsChecked(newIsChecked); // Setzt den neuen Zustand der Checkbox
    if (client) {
      client.publish({
        destination: '/app/streamGame',
        body: JSON.stringify({gameId: gameId}),
      });
      console.log("aktiviere Stream");
    }

  }



  const terminateGame = () => {
    client.publish({
      destination: '/app/terminate'
    })
  }

  return (
      <div>
        <h2 className={"ub1"}>Select Your Deck</h2>
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
        {selectedDeck && <p className={"p1"}>Waiting for opponent...</p>}
      </div>
  );
};

export default DeckSelection;
