import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Card from "../card";
import { WebSocketContext } from '../../WebSocketProvider';
import './Livestream.css';

const Livestream = () => {
    const navigate = useNavigate();
    const { client, game, setGame, users, setUsers, connected } = useContext(WebSocketContext);
    const [id, setId] = useState(null);
    const [timer, setTimer] = useState(120);
    const [playerState, setPlayerState] = useState(null);
    const [opponentState, setOpponentState] = useState(null);
    const [currentTurn, setCurrentTurn] = useState(game?.currentTurn);

    // User ID abrufen
    useEffect(() => {
        if (id === null) {
            setId(localStorage.getItem('id'));
        }
    }, [id]);

    // Initialisieren des Spielerzustands aus dem übergebenen Zustand
    useEffect(() => {
        if (users && game) {
            const currentUser = users.find(user => user.id === parseInt(id));
            const opponentUser = users.find(user => user.id !== parseInt(id));
            setCurrentTurn(game.currentTurn);

            if (currentUser) {
                setPlayerState(currentUser.playerState);
            }
            if (opponentUser) {
                setOpponentState(opponentUser.playerState);
            }
        }
    }, [id]);

    useEffect(() => {
        if (client && connected && id) {
            const subscription = client.subscribe(`/user/${id}/queue/timer`, (message) => {
                const response = JSON.parse(message.body);
                setTimer(response);
            });

            // Cleanup Subscription
            return () => {
                if (subscription) subscription.unsubscribe();
            };
        }
    }, [client, connected, id]);

    useEffect(() => {
        if (client && connected && id) {
            const subscription = client.subscribe(`/user/${id}/queue/game`, (message) => {
                const response = JSON.parse(message.body);
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
                    setCurrentTurn(response[0].currentTurn);
                }
            });

            // Cleanup Subscription
            return () => {
                if (subscription) subscription.unsubscribe();
            };
        }
    }, [client, connected, id, setGame, setUsers]);

    const handleLeaveButton = () => {
        if (client) {
            client.publish({
                destination: '/leaveStream',
                body: JSON.stringify({userId: id})
            })
        }
        navigate('/streams');
    }


    return (
        <div className="livestream-container">
            <div className="timer-and-turn">
                <div className="timer">
                    <h4>{timer} seconds</h4>
                </div>
                <div className="current-turn">
                    <h4>{users[currentTurn]?.username}</h4>
                </div>
            </div>
            <div className="life-points">
                <div className="opponent-info">
                    <h4>{users[1]?.username}</h4>
                    <div className="opponent-lp">
                        <h4>LP: {opponentState?.lifePoints}</h4>
                    </div>
                </div>
                <div className="player-info">
                    <div className="player-lp">
                        <h4>LP: {playerState?.lifePoints}</h4>
                    </div>
                    <h4>{users[0]?.username}</h4>
                </div>
            </div>
            <div className="field">
                <div className="field-row opponent-field">
                    {opponentState?.fieldCards?.slice().reverse().map((playerCard) => (
                        <div key={playerCard.id} className="card-slot">
                            <Card className="duel-card opponent-card" card={playerCard} />
                        </div>
                    ))}
                </div>
                <div className="field-row player-field">
                    {playerState?.fieldCards?.map((playerCard) => (
                        <div key={playerCard.id} className="card-slot">
                            <Card className="duel-card" card={playerCard} />
                        </div>
                    ))}
                </div>
            </div>
            <button onClick={() => handleLeaveButton()} className="leave-button">Verlassen</button>
        </div>
    );
};

export default Livestream;
