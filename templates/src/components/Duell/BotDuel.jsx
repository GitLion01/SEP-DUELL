import React, {useContext, useEffect, useState} from 'react';
import { toast, ToastContainer } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import Card from "../card";
import {WebSocketContext} from "../../WebSocketProvider";
import './duel.css'
import SwapModal from "./SwapModal";
import StatisticsModal from "./StatisticsModal";
import ConfirmModal from "./ConfirmModal";

const Duel = () => {
    const navigate = useNavigate();
    const { client, game, setGame, users, setUsers, botPS, setBotPS, connected } = useContext(WebSocketContext); // Daten aus dem Kontext
    const [id, setId] = useState(null);
    const [gameId, setGameId]= useState(null);
    const [timer, setTimer] = useState(120);
    const [playerState, setPlayerState] = useState(null);
    const [opponentState, setOpponentState] = useState(null);
    const [isSetCardMode, setIsSetCardMode] = useState(false);
    const [isRareSwapMode, setIsRareSwapMode] = useState(false);
    const [isLegendarySwapMode, setIsLegendarySwapMode] = useState(false);
    const [selectedAttacker, setSelectedAttacker] = useState(null);
    const [selectedTarget, setSelectedTarget] = useState(null);
    const [myTurn, setMyTurn] = useState(game?.myTurn);
    const [hasAttacked, setHasAttacked] = useState(false);
    const [selectedCards, setSelectedCards] = useState([]);
    const [selectedHandCard, setSelectedHandCard] = useState(null);
    const [stats, setStats] = useState(null);
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);


    // user ID abrufen
    useEffect(() => {
        if (id === null) {
            setId(localStorage.getItem('id'));
            setGameId(localStorage.getItem('gameId'));
            console.log("neu", localStorage.getItem('gameId'));
        }
    }, []);

    //Timer
    useEffect(() => {
        if (timer > 0) {

            const interval = setInterval(() => {
                setTimer((prev) => {
                    if (prev === 11) {
                        toast.warning('10 seconds remaining!');
                    }
                    if (prev === 1) {
                        toast.warning('GAME OVER');
                        handleConfirmSurrender()
                        clearInterval(interval);
                    }
                    return prev - 1;
                });
            }, 1000);

            return () => clearInterval(interval);
        }
    }, [timer, id]);


    // Initialisieren des Spielerzustands aus dem übergebenen Zustand
    useEffect(() => {
        if (users && game) {
            console.log('user: ', users);
            const currentUser = users;
            setMyTurn(game.myTurn);

            setPlayerState(currentUser.playerState);
            setOpponentState(botPS);
            resetAttackMode();
            //setCardDrawn(false);

            console.log('playerState nach initial', playerState);
            console.log('opponent state nach intial: ', opponentState);
        }
    }, [id, connected]);

    // Channel für Timer reset
    useEffect(() => {
        if (client && connected && id) {
            const subscription = client.subscribe(`/user/${id}/queue/newTurn`, (message) => {
                const response = JSON.parse(message.body);
                console.log("Response from newTurn: ", response);

                if (response) {
                    setTimer(120);
                    toast.warning("Du bist am Zug!");
                }

            });

            // Cleanup Subscription
            return () => {
                if (subscription) subscription.unsubscribe();
            };
        }
    }, [id]);

    // Game Kanal
    useEffect(() => {
        if (client && connected && id) {
            const subscription = client.subscribe(`/user/${id}/queue/game`, (message) => {
                const response = JSON.parse(message.body);
                console.log("Response: ", response);
                console.log("ResLength: ", response.length);

                if (response.length === 3) {

                    const currentUser = response[1][0];

                    setGame(response[0]);
                    setUsers(response[1][0]);
                    setBotPS(response[2]);

                    setPlayerState(currentUser.playerState);
                    setOpponentState(response[2]);
                    setMyTurn(response[0].myTurn);

                }

                // Empfangene Daten beim Ende des Duells verarbeiten TODO Statistiken noch zu überprüfen!!!
                if (response.length > 3) {
                    const [_, __, ___, sepCoins, leaderBoardPointsWinner, leaderBoardPointsLoser, damageWinner, damageLoser, cardsPlayedA, cardsPlayedB, sacrificedA, sacrificedB] = response;
                    setStats({
                        sepCoins,
                        leaderboardPointsA: response[1][0].playerState.winner ? leaderBoardPointsWinner : leaderBoardPointsLoser,
                        leaderboardPointsB : response[1][0].playerState.winner ? leaderBoardPointsLoser : leaderBoardPointsWinner,
                        damageA: response[1][0].playerState.winner ? damageWinner : damageLoser,
                        damageB: response[1][0].playerState.winner ? damageLoser : damageWinner,
                        cardsPlayedA,
                        cardsPlayedB,
                        sacrificedA,
                        sacrificedB
                    })
                    console.log(stats);

                }
                console.log("Karten auf Hand: ", response[1][0].playerState.handCards);
                console.log("Lebenspunkte: ", response[1][0].playerState.lifePoints);

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

        if (client) {
            client.publish({
                destination: '/app/endTurn',
                body: JSON.stringify({ gameID: gameId, userID: id }),
            });

            resetAttackMode()
            setHasAttacked(false);
        }
    };

    // Angriff
    const handleAttack = () => {

        if (!myTurn) {
            toast.warning("Du bist nicht am Zug");
            resetAttackMode();
            return;
        }

        if (opponentState.fieldCards.length === 0) {
            console.log("der sollte jetzt angreifen");

            console.log("angreifer wurde ausgewählt");
            client.publish({
                destination: '/app/attackBot',
                body: JSON.stringify({
                    gameId: gameId,
                    attackerId: id,
                    botPSId: opponentState.id,
                    attackerCardId: selectedAttacker
                }),
            });
            console.log("Mein feld vor Angriff: ", playerState.fieldCards);
            console.log("Gegner feld vor Angriff: ", opponentState.fieldCards);
            setHasAttacked(true);
        }
        else {
            console.log(id, selectedAttacker, selectedTarget);
            //if (selectedTarget && selectedAttacker) {
            console.log("gegnerkarte wird angegriffen");
            client.publish({
                destination: '/app/attackBotCard',
                body: JSON.stringify({
                    gameId: gameId,
                    userIdAttacker: id,
                    botPSId: opponentState.id,
                    attackerId: selectedAttacker,
                    targetId: selectedTarget
                }),
            });
            setHasAttacked(true);
        }

    }

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

        if (!myTurn) {
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

        if (!myTurn) {
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
        if (!myTurn) {
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
        sessionStorage.removeItem('botPS');
        navigate('/startseite')
    }

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
                    userId: opponentState.id,
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
                    <h4>{myTurn ? users?.username : "CPU"}</h4>
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
                    {playerState?.fieldCards.map((playerCard) => (
                        <div key={playerCard.id} className="card-slot">
                            <Card className="duel-card" card={playerCard} onCardClick={() => selectAttackingCard(playerCard.id)} />
                        </div>
                    ))}
                </div>
            </div>
            <div className="hand player-hand">
                {playerState?.handCards.map((playerCard) => (
                    <div key={playerCard.id} className="card">
                        <Card className="duel-card" card={playerCard} onCardClick={() => handleSetCard(playerCard.id)} />
                    </div>
                ))}
            </div>
            <div className="player-actions">
                {/* <button onClick={() => handleDrawCard()}>Karte Ziehen</button> */}
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