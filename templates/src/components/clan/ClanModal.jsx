import React, { useContext, useState, useEffect } from 'react';
import './ClanModal.css'; // Optional: CSS for styling
import { ToastContainer, toast } from 'react-toastify';
import { WebSocketContext } from '../../WebSocketProvider';

const ClanModal = ({ clan, onClose, userId, userClanId, joinClan, leaveClan }) => {
    const [clanMembers, setClanMembers] = useState([]);
    const { startTournament } = useContext(WebSocketContext);
    
    const fetchClanMembers = async () => {
        try {
            const response = await fetch(`http://localhost:8080/getClanMitglieder?clanId=${clan.id}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setClanMembers(data);
        } catch (error) {
            console.error('There was a problem with the fetch operation:', error);
            toast.error('Es gab einen Fehler beim Aufruf der Mitglieder');
        }
    };

    useEffect(() => {
        fetchClanMembers();
    }, [clan.id]);

    const handleJoinClan = async () => {
        await joinClan(clan.id, userId);
        fetchClanMembers();
    };

    const handleLeaveClan = async () => {
        await leaveClan(userId);
        fetchClanMembers();
        onClose();
    };
    
    const handleTournamentStart = () => {
        startTournament(clan.id);
    };

    return (
        <div className="clan-modal-content">
            <span className="close" onClick={onClose}>&times;</span>
            <h2>{clan.name} Mitglieder</h2>
            <div className="clan-members">
                {clanMembers.map((member) => (
                    <div key={member.id} className="clan-member">
                        {member.username}
                    </div>
                ))}
            </div>
            {userClanId !== clan.id && userClanId === null && (
                <button onClick={handleJoinClan}>
                    Beitreten
                </button>
            )}
            {userClanId === clan.id && (
                <>
                    <button onClick={handleTournamentStart}>
                        Turnier starten
                    </button>
                    <button onClick={handleLeaveClan}>
                        Clan verlassen
                    </button>
                </>
            )}
        </div>
    );
};

export default ClanModal;
