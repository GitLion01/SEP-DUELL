import React, { useContext, useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { DuelContext } from './DuelContext';
import { toast, ToastContainer } from 'react-toastify';


const Duel = () => {
  const { state, dispatch } = useContext(DuelContext);
  const [client, setClient] = useState(null);
  const [timer, setTimer] = useState(120);
  const [currentPlayer, setCurrentPlayer] = useState(state.currentPlayer);
  const [playerState, setPlayerState] = useState({ handCards: [], fieldCards: [], life: 50 });
  const [opponentState, setOpponentState] = useState({ handCards: [], fieldCards: [], life: 50 });
  const [gameId, setGameId] = useState(localStorage.getItem('gameId'));
  const id = localStorage.getItem('id');

  useEffect(() => {
    const newClient = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      connectHeaders: {
        login: 'guest',
        passcode: 'guest',
      },
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      onConnect: () => {
        newClient.subscribe('/topic/game', (message) => {
          const action = JSON.parse(message.body);
          switch (action.type) {
            case 'STATE_UPDATE':
              setPlayerState(action.payload.playerState);
              setOpponentState(action.payload.opponentState);
              setCurrentPlayer(action.payload.currentPlayer);
              break;
            case 'END_TURN':
              startNewTurn(action.payload.nextPlayer);
              break;
            default:
              break;
          }
        });
      },
    });

    newClient.activate();
    setClient(newClient);

    return () => {
      if (client) {
        client.deactivate();
      }
    };
  }, [client]);

  useEffect(() => {
    if (timer > 0 && currentPlayer === id) {
      const interval = setInterval(() => {
        setTimer((prev) => {
          if (prev === 11) {
            toast.warning('10 seconds remaining!');
          }
          if (prev === 1) {
            handleTimeout();
            clearInterval(interval);
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(interval);
    }
  }, [timer, currentPlayer, id]);

  const startNewTurn = (player) => {
    setCurrentPlayer(player);
    setTimer(120);
    if (client) {
      client.publish({
        destination: '/app/drawCard',
        body: JSON.stringify({ userID: id, gameID: gameId }),
      });
    }
  };

  const handleTimeout = () => {
    toast.error('Time is up! You lost the duel.');
    if (client) {
      client.publish({
        destination: '/app/timeout',
        body: JSON.stringify({ userID: id, gameID: gameId }),
      });
    }
  };

  const handleAttack = (attackingCardId, targetId) => {
    if (client) {
      client.publish({
        destination: '/app/attackCard',
        body: JSON.stringify({
          gameID: gameId,
          attackingCardId,
          targetId,
        }),
      });
    }
  };

  const handleEndTurn = () => {
    if (client) {
      client.publish({
        destination: '/app/endTurn',
        body: JSON.stringify({ gameID: gameId, userID: id }),
      });
    }
  };

  return (
    <div className="duel-container">
      <div className="timer">
        <h4>Time remaining: {timer} seconds</h4>
      </div>
      <div className="player-field">
        <h3>Your Field</h3>
        <div className="cards">
          {state.playerCards.map(card => (
            <div key={card.id} className="card">
              <div>{card.name}</div>
              <div>ATK: {card.attack}</div>
              <div>DEF: {card.defense}</div>
              <button onClick={() => handleAttack(card.id, 'opponentId')}>Attack</button>
            </div>
          ))}
        </div>
        <div>
          <button onClick={handleEndTurn}>End Turn</button>
        </div>
      </div>
      <div className="opponent-field">
        <h3>Opponent's Field</h3>
        <div className="cards">
          {state.opponentCards.map(card => (
            <div key={card.id} className="card">
              <div>{card.name}</div>
              <div>ATK: {card.attack}</div>
              <div>DEF: {card.defense}</div>
            </div>
          ))}
        </div>
      </div>
      <ToastContainer />
    </div>
  );
};

export default Duel;
