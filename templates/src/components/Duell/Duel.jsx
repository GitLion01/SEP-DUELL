import React, { useEffect, useState } from 'react';
import { toast, ToastContainer } from 'react-toastify';
import Card from "../card";


const Duel = ({client, gameId}) => {
  const [timer, setTimer] = useState(120);
  const [currentPlayer, setCurrentPlayer] = useState(null);
  const [playerState, setPlayerState] = useState({ handCards: [], fieldCards: [], life: 50 });
  const [opponentState, setOpponentState] = useState({ handCards: [], fieldCards: [], life: 50 });
  const [isAttackMode, setIsAttackMode] = useState(false);
  const [isSetCardMode, setIsSetCardMode] = useState(false); // Zustand für das Setzen von Karten
  const [selectedAttacker, setSelectedAttacker] = useState(null);
  const [selectedTarget, setSelectedTarget] = useState(null);
  const id = localStorage.getItem('id');

  useEffect(() => {
    if (client) {
      client.subscribe('/all/game', (message) => {
        const action = JSON.parse(message.body);
        useEffect(() => {
          if (client) {
            client.subscribe('/all/game', (message) => {
              const action = JSON.parse(message.body);
              if (action.game) {
                const game = action.game;
                if (game.users[0].id === id) {
                  setPlayerState(game.users[0].playerState);
                  setOpponentState(game.users[1].playerState);
                  }
                else {
                  setPlayerState(game.users[1].playerState);
                  setOpponentState(game.users[0].playerState);
                }
                setCurrentTurn(game.currentTurn);
              }
            });
          }
        }, [client]);
      });
    }
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

  const handleAttack = () => {
    if (selectedAttacker !== null) {
      if (opponentState.fieldCards.length === 0) {
        // Direkter Angriff auf den Gegner
        if (client) {
          client.publish({
            destination: '/app/attackUser',
            body: JSON.stringify({
              gameId,
              userIDAttacker: id,
              userIDDefender: opponentState.userId,
              attackerIndex: selectedAttacker
            }),
          });
        }
      } else if (selectedTarget !== null) {
        // Angriff auf gegnerische Karte
        if (client) {
          client.publish({
            destination: '/app/attackCard',
            body: JSON.stringify({
              gameID: gameId,
              attackingCardIndex: selectedAttacker,
              targetCardIndex: selectedTarget,
            }),
          });
        }
      }
      resetAttackMode();
    }
  };

  const selectAttackingCard = (index) => {
    if (isAttackMode) {
      setSelectedAttacker(index);
    }
  };

  const selectTargetCard = (index) => {
    if (isAttackMode) {
      setSelectedTarget(index);
      handleAttack(); // Führe Angriff aus, wenn Ziel ausgewählt ist
    }
  };

  const handleSetCard = (index) => {
    if (isSetCardMode) {
      client.publish({
        destination: '/app/playCard',
        body: JSON.stringify({
          gameID: gameId,
          userID: id,
          cardIndex: index
        }),
      });
      setIsSetCardMode(false); // Deaktivieren des Setzkartenmodus
    }
  };

  const handleEndTurn = () => {
    if (client) {
      client.publish({
        destination: '/app/endTurn',
        body: JSON.stringify({ gameID: gameId, userID: id }),
      });
      toast.success("Zug Beendet");
    }
  };

  const resetAttackMode = () => {
    setIsAttackMode(false);
    setSelectedAttacker(null);
    setSelectedTarget(null);
  };

  return (
      <div className="duel-container">
        <div className="timer">
          <h4>Time remaining: {timer} seconds</h4>
        </div>
        <div className="action-controls">
          <button onClick={() => setIsAttackMode(true)}>Angreifen</button>
          {isAttackMode && toast.success("Angriffsmodus aktiviert.")}
          <button onClick={handleEndTurn}>End Turn</button>
        </div>
        <div className="player-field">
          <h3>Your Field</h3>
          <div className="cards">
            {playerState.fieldCards.map((card, index) => (
                <div key={index} className="card">
                  <Card card={card} onClick={() => selectAttackingCard(index)}/>
                </div>
            ))}
          </div>
        </div>
        <div className="player-hand">
          <h3>Your Hand</h3>
          <div className="cards">
            {playerState.handCards.map((card, index) => (
                <div key={index} className="card">
                  <Card card={card} onClick={() => handleSetCard(index)}/>
                </div>
            ))}
          </div>
        </div>
        <div className="opponent-field">
          <h3>Opponent's Field</h3>
          <div className="cards">
            {opponentState.fieldCards.map((card, index) => (
                <div key={index} className="card">
                  <Card card={card} onClick={() => selectTargetCard(index)}/>
                </div>
            ))}
          </div>
        </div>
        <ToastContainer/>
      </div>
  );
};

export default Duel;
