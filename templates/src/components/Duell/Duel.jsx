import React, { useEffect, useState } from 'react';
import { toast, ToastContainer } from 'react-toastify';
import Card from "../card";


const Duel = ({client, gameId, game}) => {
  const [timer, setTimer] = useState(120);
  const [playerState, setPlayerState] = useState(null);
  const [opponentState, setOpponentState] = useState(null);
  const [isAttackMode, setIsAttackMode] = useState(false);
  const [isSetCardMode, setIsSetCardMode] = useState(false); // Zustand für das Setzen von Karten
  const [selectedAttacker, setSelectedAttacker] = useState(null);
  const [selectedTarget, setSelectedTarget] = useState(null);
  const [currentTurn, setCurrentTurn] = useState(0); // currentTurn-Index
  const id = localStorage.getItem('id');

  useEffect(() => {
    if (game.users[0].id === id) {
      setPlayerState(game.users[0].playerState);
      setOpponentState(game.users[1].playerState);
    }
    else {
      setPlayerState(game.users[1].playerState);
      setOpponentState(game.users[0].playerState);
    }
  }, [id]);

  useEffect(() => {
    if (client) {
      client.subscribe(`/user/${id}/queue/game`, (message) => {
        const action = JSON.parse(message.body);
        if (action) {
          const game = action;
          if (action.users[0].id === id) {
            setPlayerState(game.users[0].playerState);
            setOpponentState(game.users[1].playerState);
          }
          else {
            setPlayerState(game.users[1].playerState);
            setOpponentState(game.users[0].playerState);
          }
          if (game.currentTurn !== currentTurn) {
            setCurrentTurn(game.currentTurn);
            resetTimer(); // Setze den Timer zurück, wenn sich der currentTurn ändert
          }        }
      });
    }
  }, [client]);

  useEffect(() => {
    if (timer > 0) {
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
  }, [timer, currentTurn, id]);

  const resetTimer = () => {
    setTimer(120);
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
          <button onClick={() => setIsSetCardMode(true)}>Karte einsetzen</button>
          <button onClick={() => setIsAttackMode(true)}>Angreifen</button>
          {isAttackMode && toast.success("Angriffsmodus aktiviert.")}
          <button onClick={handleEndTurn}>End Turn</button>
        </div>
        <div className="player-field">
          <h3>Your Field</h3>
          <div className="cards">
            {playerState.fieldCards.map((card, index) => (
                <div key={index} className="card">
                  <Card card={card} onCardClick={() => selectAttackingCard(index)}/>
                </div>
            ))}
          </div>
        </div>
        <div className="player-hand">
          <h3>Your Hand</h3>
          <div className="cards">
            {playerState.handCards.map((card, index) => (
                <div key={index} className="card">
                  <Card card={card} onCardClick={() => handleSetCard(index)}/>
                </div>
            ))}
          </div>
        </div>
        <div className="opponent-field">
          <h3>Opponent's Field</h3>
          <div className="cards">
            {opponentState.fieldCards.map((card, index) => (
                <div key={index} className="card">
                  <Card card={card} onCardClick={() => selectTargetCard(index)}/>
                </div>
            ))}
          </div>
        </div>
        <ToastContainer/>
      </div>
  );
};

export default Duel;
