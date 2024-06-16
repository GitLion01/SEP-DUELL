import React from 'react';
import './Notification.css';

const Notification = ({ challenger, onAccept, onReject, type }) => {
    if (type !== 'challenge') {     //type für den countdown
        return null;
    }

    return (
        <div className="notification">
            <p>{challenger} hat dich zu einem Duell herausgefordert!</p>
            <button onClick={() => onAccept(challenger)}>Akzeptieren</button>
            <button onClick={() => onReject(challenger)}>Ablehnen</button>
        </div>
    );
};

export default Notification;
