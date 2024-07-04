import React, { useContext } from 'react';
import { WebSocketContext } from '../../WebSocketProvider';
import TournamentNotification from './TournamentNotification';
import '../LeaderboardPage/GlobalNotification.css';

const GlobalTournamentNotification = () => {
    const { notifications, acceptTournament, rejectTournament } = useContext(WebSocketContext);
    const userId = localStorage.getItem('id')

    return (
        <div className="global-notification">
            {notifications
                .filter(notification => notification.message === 'turnier')
                .map((notification, index) => (
                    <TournamentNotification
                        key={index}
                        onAccept={() => acceptTournament(userId)}
                        onReject={() => rejectTournament(userId)}
                    />
                ))}
        </div>
    );
};

export default GlobalTournamentNotification;
