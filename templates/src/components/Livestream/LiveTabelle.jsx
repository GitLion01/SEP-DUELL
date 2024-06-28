import React, {useState, useEffect, useContext} from 'react';
import axios from 'axios';
import BackButton from '../BackButton';
import "./LiveTabelle.css";
import { WebSocketContext } from "../../WebSocketProvider";

const LiveTabelle = () => {

    const [id, setId] = useState(null);
    const { client, connected } = useContext(WebSocketContext);
    const [liveGames, setLiveGames] = useState({});

    useEffect(() => {
        const userId = localStorage.getItem('id');
        if (userId && id === null) {
            setId(userId);
        } else {
            console.error('Keine Benutzer-ID im LocalStorage gefunden.');
        }
    }, []);

    useEffect(() => {
        const loadStreams = async () => {
            if (id !== null) {
                try {
                    const streamsResponse = await axios.get(`http://localhost:8080/initialStreams`);
                    setLiveGames(streamsResponse.data);
                    console.log(streamsResponse);
                    console.log(liveGames);
                }
                catch (error) {
                    console.log("Spiele konnten nicht geladen werden", error);
                }
            }
        }
        loadStreams();


    }, [id]);


    useEffect(() => {
        if (client && connected) {
            const subscription = client.subscribe(`/queue/streams`, (message) => {
                const gamesMap = JSON.parse(message.body);
                setLiveGames(gamesMap);
                console.log("neue streams", gamesMap);
            });

            return () => subscription.unsubscribe();
        }
    }, [id, client, connected]);

    return (
        <div>
            <BackButton />
            <h2 className={"Ueberschrift"}>Aktuelle Spiele</h2>
            <BackButton/>
            <div className="live-games-list">
                {Object.entries(liveGames).map(([gameId, players]) => (
                    <div key={gameId} className="live-game-item">
                        <span>{players[0]} vs {players[1]}</span>
                        <button /*onClick={} */>Zuschauen</button>
                    </div>
                ))}
            </div>
        </div>
    )

};

export default LiveTabelle;