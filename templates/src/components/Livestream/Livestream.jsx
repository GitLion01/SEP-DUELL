import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Card from "../card";
import { WebSocketContext } from '../../WebSocketProvider';
import './Livestream.css';
import StatisticsModal from "../Duell/StatisticsModal";

const Livestream = () => {
    const navigate = useNavigate();
    const { client, game, setGame, users, setUsers, connected } = useContext(WebSocketContext);
    const [id, setId] = useState(null);
    const [timer, setTimer] = useState(120);
    const [user1State, setUser1State] = useState(null);
    const [user2State, setUser2State] = useState(null);
    const [currentTurn, setCurrentTurn] = useState(game?.currentTurn);
    const [stats, setStats] = useState(null);


    // User ID abrufen
    useEffect(() => {
        if (id === null) {
            setId(localStorage.getItem('id'));
        }
    }, [id]);

    // Initialisieren des Spielerzustands aus dem übergebenen Zustand
    useEffect(() => {
        if (users && game) {
            const user1 = users[0];
            const user2 = users[1]
            setCurrentTurn(game.currentTurn);

            if (user1) {
                setUser1State(user1.playerState);
            }
            if (user2) {
                setUser2State(user2.playerState);
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
            const subscription = client.subscribe(`/user/${id}/queue/watch`, (message) => {
                const response = JSON.parse(message.body);
                console.log("Response Stream: ", response);
                if (response.length === 2) {
                    const user1 = response[1][0];
                    const user2 = response[1][1];

                    setGame(response[0]);
                    setUsers(response[1]);

                    if (user1) {
                        setUser1State(user1.playerState);
                    }
                    if (user2) {
                        setUser2State(user2.playerState);
                    }
                    setCurrentTurn(response[0].currentTurn);
                }

                if (response.length > 3) {

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
                destination: '/app/leaveStream',
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
                        <h4>LP: {user2State?.lifePoints}</h4>
                    </div>
                </div>
                <div className="player-info">
                    <div className="player-lp">
                        <h4>LP: {user1State?.lifePoints}</h4>
                    </div>
                    <h4>{users[0]?.username}</h4>
                </div>
            </div>
            <div className="field">
                <div className="field-row opponent-field">
                    {user2State?.fieldCards?.slice().reverse().map((playerCard) => (
                        <div key={playerCard.id} className="card-slot">
                            <Card className="duel-card opponent-card" card={playerCard} />
                        </div>
                    ))}
                </div>
                <div className="field-row player-field">
                    {user1State?.fieldCards?.map((playerCard) => (
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
