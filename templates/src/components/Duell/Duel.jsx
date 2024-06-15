import React, {useContext, useEffect, useState} from 'react';
import { toast, ToastContainer } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import Card from "../card";
import {WebSocketContext} from "../../WebSocketProvider";
import './duel.css';


const Duel = () => {
  const navigate = useNavigate();
  const { client, game, setGame, users, setUsers } = useContext(WebSocketContext);
  const id = localStorage.getItem('id');
  const gameId = localStorage.getItem('gameId');
  const [timer, setTimer] = useState(120);
  const [playerState, setPlayerState] = useState(null);
  const [opponentState, setOpponentState] = useState(null);
  const [isAttackMode, setIsAttackMode] = useState(false);
  const [isSetCardMode, setIsSetCardMode] = useState(false);
  const [selectedAttacker, setSelectedAttacker] = useState(null);
  const [selectedTarget, setSelectedTarget] = useState(null);
  const [currentTurn, setCurrentTurn] = useState(0);
  const [cardDrawn, setCardDrawn] = useState(false);



  // Initialisieren des Spielerzustands aus dem übergebenen Zustand
  useEffect(() => {
    if (users) {
      console.log('user: ', users);
      if (users[0].id === parseInt(id)) {
        setPlayerState(users[0].playerState);
        setOpponentState(users[1].playerState);
      } else {
        setPlayerState(users[1].playerState);
        setOpponentState(users[0].playerState);
      }
      setCurrentTurn(game.currentTurn); // initialisieren Sie den currentTurn
      console.log('playerState nach initial',playerState);
      console.log('opponent state nach intial: ',playerState);
    }

  }, [id, users, game]);

  /*
  useEffect(() => {
    if (users[0].id === id) {
      setPlayerState(users[0].playerState);
      setOpponentState(users[1].playerState);
    }
    else {
      setPlayerState(users[1].playerState);
      setOpponentState(users[0].playerState);
    }
  }, [id]);

   */

    if (client) {
      client.subscribe(`/user/${id}/queue/game`, (message) => {
        const response = JSON.parse(message.body);
        setGame(response[0]);
        setUsers(response[1]);
        if (response) {
          if (response[1][0].id === parseInt(id)) {
            setPlayerState(response[1][0].playerState);
            setOpponentState(response[1][1].playerState);
          }
          else {
            setPlayerState(response[1][1].playerState);
            setPlayerState(response[1][0].playerState);
          }
          if (response[0].currentTurn !== currentTurn) {
            setCurrentTurn(response[0].currentTurn);
            if (response[1][currentTurn].id === parseInt(id)) {
            }
            //resetTimer(); // Setze den Timer zurück, wenn sich der currentTurn ändert
          }        }
      });
    }


   /*
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

    */

  const resetTimer = () => {
    setTimer(120);
  };

  const handleEndTurn = () => {
    if (client) {
      client.publish({
        destination: '/app/endTurn',
        body: JSON.stringify({ gameID: gameId, userID: id }),
      });
      if (cardDrawn) {
        setCardDrawn(false);
      }
    }
  };

  const handleTimeout = () => {
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
          gameId: gameId,
          userId: id,
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

  const handleDrawCard = () => {
    if (!cardDrawn) {
      client.publish({
        destination: '/app/drawCard',
        body: JSON.stringify({
          gameId: gameId,
          userId: id
        })
      })
      if(users[currentTurn].id === parseInt(id)) {
        setCardDrawn(true);
      }
    }
    else { toast.error("Karte bereits gezogen");}
  }

  return (
      <div className="duel-container">
        <div className="timer">
          <h4>{timer} seconds</h4>
        </div>
        <div className="currentTurn">
          <h4>{users[currentTurn].username}</h4>
        </div>
        <div className="player-actions">
          <button onClick={() => handleDrawCard()}>Karte Ziehen</button>
          <button onClick={() => setIsSetCardMode(true)}>Karte einsetzen</button>
          <button onClick={() => setIsAttackMode(true)}>Angreifen</button>
          <button onClick={handleEndTurn}>End Turn</button>
        </div>
        <div className="field">
          <div className="field-row opponent-field">
            {opponentState?.fieldCards?.map((playerCard, index) => (
                <div key={index} className="card-slot">
                  <Card card={playerCard} onCardClick={() => selectTargetCard(index)}/>
                </div>
            ))}
          </div>
          <div className="field-row player-field">
            {playerState?.fieldCards?.map((playerCard, index) => (
                <div key={index} className="card-slot">
                  <Card card={playerCard} onCardClick={() => selectAttackingCard(index)}/>
                </div>
            ))}
          </div>
        </div>
        <div className="hand player-hand">
          {playerState?.handCards?.map((playerCard, index) => (
              <div key={index} className="card">
                <Card card={playerCard} onCardClick={() => handleSetCard(index)}/>
              </div>
          ))}
        </div>
        <div className="life-points">
          <div className="opponent-lp">
            <h4>LP: {opponentState?.lifePoints}</h4>
          </div>
          <div className="player-lp">
            <h4>LP: {playerState?.lifePoints}</h4>
          </div>
        </div>
        <ToastContainer/>
      </div>
  );
};

export default Duel;
