import React, {useContext, useEffect, useState} from 'react';
import { toast, ToastContainer } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import Card from "../card";
import {WebSocketContext} from "../../WebSocketProvider";
import './duel.css';
import SwapModal from "./SwapModal";
import StatisticsModal from "./StatisticsModal";
import ConfirmModal from "./ConfirmModal";


const Duel = () => {
  const navigate = useNavigate();
  const { client, game, setGame, users, setUsers, connected } = useContext(WebSocketContext); // Daten aus dem Kontext
  const [id, setId] = useState(null);
  const gameId = localStorage.getItem('gameId');
  const [timer, setTimer] = useState(120);
  const [playerState, setPlayerState] = useState(null);
  const [opponentState, setOpponentState] = useState(null);
  const [isSetCardMode, setIsSetCardMode] = useState(false);
  const [isRareSwapMode, setIsRareSwapMode] = useState(false);
  const [isLegendarySwapMode, setIsLegendarySwapMode] = useState(false);
  const [selectedAttacker, setSelectedAttacker] = useState(null);
  const [selectedTarget, setSelectedTarget] = useState(null);
  const [currentTurn, setCurrentTurn] = useState(game?.currentTurn);
  const [hasAttacked, setHasAttacked] = useState(false);
  const [selectedCards, setSelectedCards] = useState([]);
  const [selectedHandCard, setSelectedHandCard] = useState(null);
  const [stats, setStats] = useState(null);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);



  // user ID abrufen
  useEffect(() => {
    if (id === null) {
      setId(localStorage.getItem('id'));
    }
  }, []);

  useEffect(() => {
    if (timer > 0 && currentTurn.id === parseInt(id)) {

      const interval = setInterval(() => {
        setTimer((prev) => {
          if (prev === 11) {
            toast.warning('10 seconds remaining!');
          }
          if (prev === 1 && currentTurn.id === parseInt(id)) {
            toast.warning('GAME OVER');
            handleConfirmSurrender()
            clearInterval(interval);
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(interval);
    }
  }, [timer, currentTurn, id]);

  useEffect(() => {
    if (client && connected && id) {
      const subscription = client.subscribe(`/user/${id}/queue/newTurn`, (message) => {
        const response = JSON.parse(message.body);
        console.log(response);

        if (response) {
          setCurrentTurn(response);
          setTimer(120);
        }

      });

      // Cleanup Subscription
      return () => {
        if (subscription) subscription.unsubscribe();
      };
    }
  }, [id]);

  // Initialisieren des Spielerzustands aus dem übergebenen Zustand
  useEffect(() => {
    if (users && game) {
      console.log('user: ', users);
      const currentUser = users.find(user => user.id === parseInt(id));
      const opponentUser = users.find(user => user.id !== parseInt(id));
      setCurrentTurn(game.currentTurn);

      if (currentUser) {
        setPlayerState(currentUser.playerState);
      }
      if (opponentUser) {
        setOpponentState(opponentUser.playerState);
      }
      resetAttackMode();

      console.log('playerState nach initial', playerState);
      console.log('opponent state nach intial: ', opponentState);
    }
  }, [id, connected]);




  // Game Kanal
  useEffect(() => {
    if (client && connected && id) {
      const subscription = client.subscribe(`/user/${id}/queue/game`, (message) => {
        const response = JSON.parse(message.body);
        console.log("Response: ", response);

        if (response.length === 2) {
          const currentUser = response[1].find(user => user.id === parseInt(id));
          const opponentUser = response[1].find(user => user.id !== parseInt(id));

          setGame(response[0]);
          setUsers(response[1]);

          if (currentUser) {
            setPlayerState(currentUser.playerState);
          }
          if (opponentUser) {
            setOpponentState(opponentUser.playerState);
          }

          console.log("Mein feld nach Angriff: ", currentUser.playerState.fieldCards);
          console.log("Gegner feld nach Angriff: ", opponentUser.playerState.fieldCards);
        }

        // Empfangene Daten beim Ende des Duells verarbeiten
        if (response.length > 3) {
          const [_, __, sepCoins, leaderBoardPointsWinner, leaderBoardPointsLoser, damageWinner, damageLoser, cardsPlayedA, cardsPlayedB, sacrificedA, sacrificedB] = response;
          setStats({
            sepCoins,
            leaderboardPointsA: response[1][0].playerState.winner ? leaderBoardPointsWinner : leaderBoardPointsLoser,
            leaderboardPointsB : response[1][1].playerState.winner ? leaderBoardPointsWinner : leaderBoardPointsLoser,
            damageA: response[1][0].playerState.winner ? damageWinner : damageLoser,
            damageB: response[1][1].playerState.winner ? damageWinner : damageLoser,
            cardsPlayedA,
            cardsPlayedB,
            sacrificedA,
            sacrificedB
          })
          console.log(stats);

        }

      });

      // Cleanup Subscription
      return () => {
        if (subscription) subscription.unsubscribe();
      };
    }
  }, [client, connected, id]);



  // Zug beenden
  const handleEndTurn = () => {

    console.log("Karten im Deck: ", playerState.deckClone.length);

    if (currentTurn.id !== parseInt(id)) {
      toast.warning("Du bist nicht am Zug");
      resetAttackMode();
      return;
    }

    if (client) {
      client.publish({
        destination: '/app/endTurn',
        body: JSON.stringify({ gameID: gameId, userID: id }),
      });

      resetAttackMode()
      setHasAttacked(false);
    }
  };

  const handleAttack = () => {

    if (currentTurn.id !== parseInt(id)) {
      toast.warning("Du bist nicht am Zug");
      resetAttackMode();
      return;
    }

    if (opponentState.fieldCards.length === 0) {
      console.log("der sollte jetzt angreifen");

      console.log("angreifer wurde ausgewählt");
      client.publish({
        destination: '/app/attackUser',
        body: JSON.stringify({
          gameId: gameId,
          attackerId: id,
          defenderId: users.find(user => user.id !== parseInt(id)).id,
          attackerCardId: selectedAttacker
        }),
      });
      console.log("Mein feld vor Angriff: ", playerState.fieldCards);
      console.log("Gegner feld vor Angriff: ", opponentState.fieldCards);
      setHasAttacked(true);
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
          userIdDefender: users.find(user => user.id !== parseInt(id)).id,
          attackerId: selectedAttacker,
          targetId: selectedTarget
        }),
      });
      setHasAttacked(true);
    }
  };

  const selectAttackingCard = (Id) => {
    setSelectedAttacker(Id);
    console.log("angreifende karte wird ausgewählt: ", id);
  };

  useEffect(() => {
    if (selectedAttacker !== null) {
      console.log('Selected Attacker:', selectedAttacker);
    }
  }, [selectedAttacker]);

  useEffect(() => {
    if (selectedTarget !== null) {
      console.log('Selected Target:', selectedTarget);
    }
  }, [selectedTarget]);

  const selectTargetCard = (Id) => {
    setSelectedTarget(Id);
    console.log("jetzt wird die karte angegriffen ", selectedTarget);

  };

  const handleSetCard = (Id) => {

    if (currentTurn.id !== parseInt(id)) {
      toast.warning("Du bist nicht am Zug");
      resetAttackMode();
      return;
    }

    if (hasAttacked) {
      toast.error("Sie haben schon angegriffen");
      return;
    }

    if (isSetCardMode) {
      console.log("karte wird gesetzt: ", Id);
      client.publish({
        destination: '/app/placeCard',
        body: JSON.stringify({
          gameId: gameId,
          userId: id,
          cardId: Id
        }),
      });
      setIsSetCardMode(false); // Deaktivieren des Setzkartenmodus
      resetAttackMode();
    }
  };

  //Angriffsmodus und Zustände zurücksetzen
  const resetAttackMode = () => {
    setSelectedAttacker(null);
    setSelectedTarget(null);
  };


  const handleRareSwap = () => {

    if (currentTurn.id !== parseInt(id)) {
      toast.warning("Du bist nicht am Zug");
      resetAttackMode();
      return;
    }

    if (hasAttacked) {
      toast.error("Sie dürfen nach einem Angriff keine Karte setzen");
      return;
    }
    console.log(selectedHandCard);

    if (selectedCards.length === 2 && selectedHandCard !== null && playerState.handCards.find(playerCard => playerCard.id === selectedHandCard).rarity === 'RARE') {
      console.log("normalCardsIndex ", selectedCards);
      console.log("selectedHandCard ", selectedHandCard);

      client.publish({
        destination: '/app/rareSwap',
        body: JSON.stringify({
          gameId: gameId,
          userId: id,
          cardIds: selectedCards,
          rareId: selectedHandCard
        }),
      });
      setIsRareSwapMode(false);
      setSelectedCards([]);
      setSelectedHandCard(null);
    } else {
      toast.error("Wählen Sie 2 Karten vom Spielfeld und eine seltene Karte aus der Hand.");
    }
  };

  const handleLegendarySwap = () => {
    if (currentTurn.id !== parseInt(id)) {
      toast.warning("Du bist nicht am Zug");
      resetAttackMode();
      return;
    }

    if (hasAttacked) {
      toast.error("Sie haben schon angegriffen");
      resetAttackMode();
      return;
    }

    if (selectedCards.length === 3 && selectedHandCard !== null && playerState.handCards.find(playerCard => playerCard.id === selectedHandCard).rarity === 'LEGENDARY') {
      console.log("normalCardsIndex ", selectedCards);
      console.log("selectedHandCard ", selectedHandCard);
      client.publish({
        destination: '/app/legendarySwap',
        body: JSON.stringify({
          gameId: gameId,
          userId: id,
          cardIds: selectedCards,
          legendaryId: selectedHandCard
        }),
      });
      console.log("Gesendete KartenIdListe ", selectedCards)
      console.log("Gesendete LegendaryCardId ", selectedHandCard)
      setIsLegendarySwapMode(false);
      setSelectedCards([]);
      setSelectedHandCard(null);
    } else {
      toast.error("Wählen Sie 3 Karten vom Spielfeld und eine legendäre Karte aus der Hand.");
    }
  };

  const closeStatisticsModal = () => {
    client.publish({
      destination: '/status/status',
      body: JSON.stringify("onlineNachGame"),
      headers:{
        'userId': id.toString()
      }
    })

    setGame(null);
    setUsers(null);
    localStorage.removeItem('gameId');
    sessionStorage.removeItem('game');
    sessionStorage.removeItem('users');
    navigate('/startseite')
  };

  const handleOpenConfirmModal = () => {
    setIsConfirmModalOpen(true);
  };

  const handleCloseConfirmModal = () => {
    setIsConfirmModalOpen(false);
  };

  const handleConfirmSurrender = () => {
    client.publish({
      destination: '/app/surrender',
      body: JSON.stringify({
            userId: id,
            gameId: gameId
          }
      )
    })
    setIsConfirmModalOpen(false);
  };

  return (
      <div className="duel-container">
        <div className="timer-and-turn">
          <div className="timer">
            <h4>{timer} seconds</h4>
          </div>
          <div className="current-turn">
            <h4>{currentTurn?.username}</h4>
          </div>
        </div>
        <div className="life-points">
          <div className="opponent-lp">
            <h4>LP: {opponentState?.lifePoints}</h4>
          </div>
          <div className="player-lp">
            <h4>LP: {playerState?.lifePoints}</h4>
          </div>
        </div>
        <div className="field">
          <div className="field-row opponent-field">
            {opponentState?.fieldCards?.slice().reverse().map((playerCard) => (
                <div key={playerCard.id} className="card-slot">
                  <Card className="duel-card opponent-card" card={playerCard} onCardClick={() => selectTargetCard(playerCard.id)} />
                </div>
            ))}
          </div>
          <div className="field-row player-field">
            {playerState?.fieldCards?.map((playerCard) => (
                <div key={playerCard.id} className="card-slot">
                  <Card className="duel-card" card={playerCard} onCardClick={() => selectAttackingCard(playerCard.id)} />
                </div>
            ))}
          </div>
        </div>
        <div className="hand player-hand">
          {playerState?.handCards?.map((playerCard) => (
              <div key={playerCard.id} className="card">
                <Card className="duel-card" card={playerCard} onCardClick={() => handleSetCard(playerCard.id)} />
              </div>
          ))}
        </div>
        <div className="player-actions">
          {/*<button onClick={() => handleDrawCard()}>Karte Ziehen</button>*/}
          <button onClick={() => setIsSetCardMode(true)}>Karte einsetzen</button>
          <button onClick={() => handleAttack()}>Angreifen</button>
          <button onClick={() => setIsRareSwapMode(true)}>Rare Swap</button>
          <button onClick={() => setIsLegendarySwapMode(true)}>Legendary Swap</button>
          <button onClick={handleEndTurn}>End Turn</button>
          <button onClick={handleOpenConfirmModal}> Aufgeben</button>
        </div>
        <SwapModal
            isOpen={isRareSwapMode}
            onRequestClose={() => setIsRareSwapMode(false)}
            onConfirm={handleRareSwap}
            title="Wähle 2 Karten zum Opfern und eine seltene Karte aus der Hand"
            selectedCards={selectedCards}
            setSelectedCards={setSelectedCards}
            playerCards={playerState?.fieldCards || []}
            handCards={playerState?.handCards || []}
            setSelectedHandCard={setSelectedHandCard}
            selectedHandCard={selectedHandCard}
            requiredFieldCards={2}
        />
        <SwapModal
            isOpen={isLegendarySwapMode}
            onRequestClose={() => setIsLegendarySwapMode(false)}
            onConfirm={handleLegendarySwap}
            title="Wähle 3 Karten zum Opfern und eine legendäre Karte aus der Hand"
            selectedCards={selectedCards}
            setSelectedCards={setSelectedCards}
            playerCards={playerState?.fieldCards || []}
            handCards={playerState?.handCards || []}
            setSelectedHandCard={setSelectedHandCard}
            selectedHandCard={selectedHandCard}
            requiredFieldCards={3}
        />
        <StatisticsModal
            isOpen={stats !== null}
            onRequestClose={closeStatisticsModal}
            stats={stats || {}}
            users={users}
        />
        <ConfirmModal
            show={isConfirmModalOpen}
            onConfirm={handleConfirmSurrender}
            onCancel={handleCloseConfirmModal}
            message="Möchten Sie wirklich aufgeben?"
        />
        <ToastContainer />
      </div>
  );



}

export default Duel;