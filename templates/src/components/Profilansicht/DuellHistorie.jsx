import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './DuellHistorie.css';
import BackButton from "../BackButton"; // Für das Styling

const DuellHistorie = () => {
    const [duelle, setDuelle] = useState([]);
    const userId = localStorage.getItem('id');

    useEffect(() => {
        axios.get(`http://localhost:8080/profile/history/${userId}`)
            .then(response => {
                console.log("Stats: ", response);
                const formattedData = response.data.map(duell => ({
                    id: duell.id,
                    spieler1Name: duell.user1,
                    spieler2Name: duell.user2,
                    spieler1Punkte: duell.user1 === duell.winner ? duell.lpwinner : duell.lploser,
                    spieler2Punkte: duell.user2 === duell.winner ? duell.lpwinner : duell.lploser,
                    winner: duell.winner
                }));
                setDuelle(formattedData);
            })
            .catch(error => console.error('Fehler beim Abrufen der Duellhistorie:', error));
    }, [userId]);

    return (
        <>
            <BackButton />
            <div className="duellhistorie-container">
                <h2 className="h2dh">Duellhistorie</h2>
                <table className="duellhistorie-table">
                    <thead>
                    <tr>
                        <th>Duell</th>
                        <th>Spieler 1</th>
                        <th>Spieler 2</th>
                        <th>Punkte Spieler 1</th>
                        <th>Punkte Spieler 2</th>
                        <th>Gewinner</th>
                    </tr>
                    </thead>
                    <tbody>
                    {duelle.slice().reverse().map((duell, index) => (
                        <tr key={index} className="duell-summary">
                            <td>{duelle.length - index}</td>
                            <td>{duell.spieler1Name}</td>
                            <td>{duell.spieler2Name}</td>
                            <td>{duell.spieler1Punkte}</td>
                            <td>{duell.spieler2Punkte}</td>
                            <td>{duell.winner}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </>
    );
};

export default DuellHistorie;
