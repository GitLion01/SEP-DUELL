import React, { useEffect, useState } from 'react';
import './Notification.css';

const Notification = ({ challenger, onAccept, onReject, message, countdown, onTimeout }) => {
    const [timeLeft, setTimeLeft] = useState(countdown);

    useEffect(() => {
        if (timeLeft > 0) {
            const timer = setInterval(() => {
                setTimeLeft(prevTime => prevTime - 1);
            }, 1000);
            return () => clearInterval(timer);
        } else if (timeLeft === 0) {
            onTimeout(challenger);  // Ruft die Methode auf, wenn der Timer 0 erreicht
        }
    }, [timeLeft, onTimeout, challenger]);

    if (message !== 'challenge') {
        return null;
    }

    return (
        <div className="notification">
            <p>{challenger} hat dich zu einem Duell herausgefordert!</p>
            <p>Verbleibende Zeit: {timeLeft} Sekunden</p>
            <button onClick={() => onAccept(challenger)}>Akzeptieren</button>
            <button onClick={() => onReject(challenger)}>Ablehnen</button>
        </div>
    );
};

export default Notification;
