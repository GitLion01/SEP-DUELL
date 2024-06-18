import React, { useEffect, useState } from 'react';
import './Notification.css';

const Notification = ({ challengerId, challengerName, receiverId, onAccept, onReject, message, countdown, onTimeout }) => {
    const [timeLeft, setTimeLeft] = useState(countdown);

    useEffect(() => {
        if (timeLeft > 0) {
            const timer = setInterval(() => {
                setTimeLeft(prevTime => prevTime - 1);
            }, 1000);
            return () => clearInterval(timer);
        } else if (timeLeft === 0) {
            onTimeout(challengerId);  // Ruft die Methode auf, wenn der Timer 0 erreicht
        }
    }, [timeLeft, onTimeout, challengerId]);

    if (message !== 'challenge') {
        return null;
    }

    return (
        <div className="notification">
            <p>{challengerName} hat dich zu einem Duell herausgefordert!</p>
            <p>Verbleibende Zeit: {timeLeft} Sekunden</p>
            <button onClick={() => onAccept(challengerId, challengerName, receiverId)}>Akzeptieren</button>
            <button onClick={() => onReject(challengerId)}>Ablehnen</button>
        </div>
    );
};

export default Notification;
