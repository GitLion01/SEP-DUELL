import React, { useState, useEffect } from 'react';
import './ClanModal.css'; // Optional: CSS für Styling
import { ToastContainer, toast } from 'react-toastify';


const ClanModal = ({ clan, onClose, userId, userClanId, setCurrentUser, joinClan, leaveClan}) => {
    const [clanMembers, setClanMembers] = useState([]);
    const fetchClanMembers = async () => {
        // Beispielhafte Clan-Mitglieder
        const members = [
            { id: 1, username: 'Player 1' },
            { id: 2, username: 'Player 2' },
            { id: 3, username: 'Player 3' },
        ];
        setClanMembers(members);
    };

    useEffect(() => {
        fetchClanMembers();
    }, [clan.id]);

    const handleJoinClan = async () => {
        await joinClan(clan.id, userId); 
        fetchClanMembers();
        toast.success('Du bist erfolgreich beigetreten');
        };

    const handleLeaveClan = async () => {
        await leaveClan(userId)
        fetchClanMembers(); 
        toast.success('Du hast den Clan erfolgreich verlassen')
        onClose(); 
    }

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
            <button onClick={handleJoinClan} disabled={currentUser.clanId === clan.id}>
                Beitreten
            </button>
            <button onClick={handleLeaveClan} disabled={currentUser.clanId !== clan.id}>
                Clan verlassen
            </button>
        </div>
    );
};

export default ClanModal;
