import React, {useContext, useEffect, useState} from 'react';
import { toast, ToastContainer } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import Card from "../card";
import {WebSocketContext} from "../../WebSocketProvider";
import './duel.css';


const Duel = () => {
  const navigate = useNavigate();
  const { client, game, setGame, users, setUsers } = useContext(WebSocketContext);
  const [id, setId] = useState(null);
  const gameId = localStorage.getItem('gameId');
  const currentUser = users.find(user => user.id === parseInt(id));
  const opponentUser = users.find(user => user.id !== parseInt(id));
  const [timer, setTimer] = useState(120);
  const [playerState, setPlayerState] = useState(null);
  const [opponentState, setOpponentState] = useState(null);
  const [isAttackMode, setIsAttackMode] = useState(false);
  const [isSetCardMode, setIsSetCardMode] = useState(false);
  const [selectedAttacker, setSelectedAttacker] = useState(null);
  const [selectedTarget, setSelectedTarget] = useState(null);
  const [currentTurn, setCurrentTurn] = useState(game.currentTurn);
  const [cardDrawn, setCardDrawn] = useState(false);

  useEffect(() => {
    if (id === null) {
      setId(localStorage.getItem('id'));
    }
  }, []);


  // Initialisieren des Spielerzustands aus dem übergebenen Zustand
  useEffect(() => {
    if (users && game) {
      console.log('user: ', users);
      const currentUser = users.find(user => user.id === parseInt(id));
      const opponentUser = users.find(user => user.id !== parseInt(id));

      if (currentUser) {
        setPlayerState(currentUser.playerState);
      }
      if (opponentUser) {
        setOpponentState(opponentUser.playerState);
      }

      // setCurrentTurn(game.currentTurn); // initialisieren Sie den currentTurn

      console.log('playerState nach initial', playerState);
      console.log('opponent state nach intial: ', opponentState);
    }
  }, [id]);

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

  useEffect(() => {
    //if (client) {
      client.subscribe(`/user/${id}/queue/game`, (message) => {
        const response = JSON.parse(message.body);

        console.log("dönerbude");
        if (message) {
          if (response[1][0].id === parseInt(id)) {
            setPlayerState(response[1][0].playerState);
            setOpponentState(response[1][1].playerState);
          }
          else {
            setPlayerState(response[1][1].playerState);
            setOpponentState(response[1][0].playerState);
          }
            setCurrentTurn(response[0].currentTurn);
            if (response[1][currentTurn].id === parseInt(id)) {
            }
            //resetTimer(); // Setze den Timer zurück, wenn sich der currentTurn ändert

        }
      });
   // }
  }, [id]);



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
      resetAttackMode()
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

  /*
  const handleAttack = () => {
    if (selectedAttacker !== null) {
      if (selectedTarget !== null) {
        // Angriff auf gegnerische Karte
        if (client) {
          client.publish({
            destination: '/app/attackCard',
            body: JSON.stringify({
              gameId: gameId,
              userIdAttacker: id,
              userIdDefender: opponentUser.id,
              attackerIndex: selectedAttacker,
              targetIndex: selectedTarget
            }),
          });
        }
      }
      resetAttackMode();
    }
  };

   */

  const handleAttack = () => {
    if (opponentState.fieldCards.length === 0) {
      console.log("der sollte jetzt angreifen");

        console.log("angreifer wurde ausgewählt");
        client.publish({
          destination: '/app/attackUser',
          body: JSON.stringify({
            gameId: gameId,
            attackerId: id,
            defenderId: opponentUser.id,
            attackerCardIndex: selectedAttacker
          }),
        });
        console.log("gegner wird angegriffen");


    }
    else {
      console.log(selectedAttacker, selectedTarget);
      //if (selectedTarget && selectedAttacker) {
        console.log("gegnerkarte wird angegriffen");
        client.publish({
          destination: '/app/attackCard',
          body: JSON.stringify({
            gameId: gameId,
            userIdAttacker: id,
            userIdDefender: opponentUser.id,
            attackerIndex: selectedAttacker,
            targetIndex: selectedTarget
          }),
        });
      //}
    }
  }

  const selectAttackingCard = (index) => {
    //if (isAttackMode) {
      setSelectedAttacker(index);
      console.log("angreifende karte wird ausgewählt")
    //}
  };

  useEffect(() => {
    if (selectedAttacker !== null) {
      console.log('Selected Attacker:', selectedAttacker);
      // Führe hier weitere Logik aus, wenn erforderlich
    }
  }, [selectedAttacker]);

  useEffect(() => {
    if (selectedTarget !== null) {
      console.log('Selected Target:', selectedTarget);
      // Führe hier weitere Logik aus, wenn erforderlich
    }
  }, [selectedTarget]);

  const selectTargetCard = (index) => {
    //if (isAttackMode) {
      setSelectedTarget(index);
      console.log("jetzt wird die karte angegriffen ", selectedTarget);
      //handleAttack(); // Führe Angriff aus, wenn Ziel ausgewählt ist
    //}
  };

  const handleSetCard = (index) => {
    if (isSetCardMode) {
      client.publish({
        destination: '/app/placeCard',
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
      console.log("cardDrawn", cardDrawn);
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
          <button onClick={() => handleAttack()}>Angreifen</button>
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
