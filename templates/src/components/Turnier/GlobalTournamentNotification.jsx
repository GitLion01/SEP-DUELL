import React, { useContext } from 'react';
import { WebSocketContext } from '../../WebSocketProvider';
import TournamentNotification from './TournamentNotification';
import '../LeaderboardPage/GlobalNotification.css';

const GlobalTournamentNotification = () => {
    const { notifications, acceptTournament, rejectTournament, removeNotification } = useContext(WebSocketContext);
    const userId = localStorage.getItem('id')

    const handleAccept = (notificationId) => {
        acceptTournament(userId);
        removeNotification(notificationId);
    };

    const handleReject = (notificationId) => {
        rejectTournament(userId);
        removeNotification(notificationId);
    };

    return (
        <div className="global-notification">
            {notifications
                .filter(notification => notification.message === 'turnier')
                .map((notification, index) => (
                    <TournamentNotification
                        key={index}
                        notificationId={notification.id}
                        onAccept={() => handleAccept(notification.id)}
                        onReject={() => handleReject(notification.id)}
                    />
                ))}
        </div>
    );
};

export default GlobalTournamentNotification;
