import React, { useContext } from 'react';
import { WebSocketContext } from '../../WebSocketProvider';
import Notification from './Notification';
import './GlobalNotification.css';

const GlobalNotification = () => {
    const { notifications, handleAcceptChallenge, handleRejectChallenge, handleTimeoutChallenge } = useContext(WebSocketContext);

    return (
        <div className="global-notification">
            {notifications.map((notification, index) => (
                <Notification
                    key={index}
                    challenger={notification.senderName}
                    onAccept={handleAcceptChallenge}
                    onReject={handleRejectChallenge}
                    message={notification.message}
                    countdown={notification.countdown}
                    onTimeout={handleTimeoutChallenge}
                />
            ))}
        </div>
    );
};

export default GlobalNotification;
