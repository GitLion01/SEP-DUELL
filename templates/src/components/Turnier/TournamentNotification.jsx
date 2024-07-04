import React from 'react';
import '../LeaderboardPage/Notification.css';

const TournamentNotification = ({ senderName, onAccept, onReject }) => {
    return (
        <div className="notification">
            <p>Dein Clan hat dich zu einem Turnier eingeladen!</p>
            <button onClick={onAccept}>Akzeptieren</button>
            <button onClick={onReject}>Ablehnen</button>
        </div>
    );
};

export default TournamentNotification;
