import React, { useContext } from 'react';
import { WebSocketContext } from '../../WebSocketProvider';
import Notification from './Notification';
import './GlobalNotification.css';

const GlobalNotification = () => {
    const { notifications, handleAcceptChallenge, handleRejectChallenge, handleTimeoutChallenge, activeDuel, createGame } = useContext(WebSocketContext);

    const handleGameCreation = () => {
        
        const duelAcceptedNotification = notifications.find(n => n.message === 'duelAccepted');
        console.log(duelAcceptedNotification)
        const senderName = duelAcceptedNotification.senderName;
        const receiverId = duelAcceptedNotification.receiverId
        createGame(receiverId, senderName)
                
    };
    return (
        <div className="global-notification">
            {notifications.map((notification, index) => (
                <Notification
                    key={index}
                    challengerId={notification.senderId}
                    challengerName={notification.senderName}
                    receiverId={notification.receiverId}
                    onAccept={handleAcceptChallenge}
                    onReject={handleRejectChallenge}
                    message={notification.message}
                    countdown={notification.countdown}
                    onTimeout={handleTimeoutChallenge}
                />
            ))}
            {activeDuel && (
                <div className="active-duel">
                    <a href="/duel">
                        <button className="active-duel-button" onClick={handleGameCreation}>Aktives Duell</button>
                    </a>
                </div>
            )}
        </div>
    );
};

export default GlobalNotification;
