import React, { useEffect, useState } from 'react';
import './Notification.css';

const Notification = ({ senderId, senderName, receiverId, onAccept, onReject, message, countdown, onTimeout }) => {
    const [timeLeft, setTimeLeft] = useState(countdown);

    useEffect(() => {
        if (timeLeft > 0) {
            const timer = setInterval(() => {
                setTimeLeft(prevTime => prevTime - 1);
            }, 1000);
            return () => clearInterval(timer);
        } else if (timeLeft === 0) {
            onTimeout(senderId);  // Ruft die Methode auf, wenn der Timer 0 erreicht
        }
    }, [timeLeft, onTimeout, senderId]);

    if (message !== 'challenge') {
        return null;
    }

    return (
        <div className="notification">
            <p>{senderName} hat dich zu einem Duell herausgefordert!</p>
            <p>Verbleibende Zeit: {timeLeft} Sekunden</p>
            <button onClick={() => onAccept(senderId, senderName, receiverId)}>Akzeptieren</button>
            <button onClick={() => onReject(senderId)}>Ablehnen</button>
        </div>
    );
};

export default Notification;
