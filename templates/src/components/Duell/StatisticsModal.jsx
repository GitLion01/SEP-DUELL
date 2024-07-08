import React from 'react';
import Modal from 'react-modal';
import './StatisticsModal.css';

Modal.setAppElement('#root');

//Hilfsfunktion zum Formatieren der Kartenzahl nach Seltenheit
const formatCardCounts = (cardCounts) => {
    const seltenheit = ['normal', 'rare', 'legendary']
    return cardCounts ? cardCounts.map((count, index) => `${count} ${seltenheit[index]}`).join(', ') : 'Keine Karten';
};

const StatisticsModal = ({ isOpen, onRequestClose, stats, users}) => {
    return (
        <Modal
            isOpen={isOpen}
            onRequestClose={onRequestClose}
            contentLabel="Game Statistics"
            className="statistics-modal"
            overlayClassName="statistics-overlay"
        >
            <h2>Ergebnisse und Statistiken</h2>
            <p>SEP-Coins des Siegers: +{stats?.sepCoins || "0"}</p>
            <div className="stats-container">
                <div className="stats-box">
                    <h3>{users[0]?.username}</h3>
                    <p>Leaderboard-Punkte: {stats?.leaderboardPointsA}</p>
                    <p>Schaden: {stats?.damageA}</p>
                    <p>Gespielte Karten: {formatCardCounts(stats?.cardsPlayedA)}</p>
                    <p>Geopferte Karten: {formatCardCounts(stats?.sacrificedA)}</p>
                </div>
                <div className="stats-box">
                    <h3>{users[1]?.username || "CPU" }</h3>
                    <p>Leaderboard-Punkte: {stats?.leaderboardPointsB || "N/A"}</p>
                    <p>Schaden: {stats?.damageB}</p>
                    <p>Gespielte Karten: {formatCardCounts(stats?.cardsPlayedB)}</p>
                    <p>Geopferte Karten: {formatCardCounts(stats?.sacrificedB)}</p>
                </div>
            </div>
            <button onClick={onRequestClose}>Beenden</button>
        </Modal>
    );
};

export default StatisticsModal;