import React, { useEffect, useState } from 'react';
import BackButton from '../BackButton';
import Modal from 'react-modal'; 
import ClanModal from './ClanModal';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './ClanList.css'; 

function Clan() {
    const [clans, setClans] = useState([]);
    const userId = localStorage.getItem('id');
    const [newClanName, setNewClanName] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedClan, setSelectedClan] = useState(null);
    const [userClanId, setUserClanId] = useState(null);  

    const fetchClans = async () => {
        try {
            const response = await fetch('http://localhost:8080/getClans');
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setClans(data);
        } catch (error) {
            console.error('There was a problem with the fetch operation:', error);
        }
    };

    const fetchClanId = async () => {
        try { 
            const response = await fetch(`http://localhost:8080/getClanId?userId=${userId}`); 
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setUserClanId(data); 
        }
        catch (error) {
            console.error('There was a problem with the fetch operation:', error);
        }
    }

    useEffect(() => {
        fetchClans();
        fetchClanId(); 
    }, []);

    const createClan = async () => {
        if (newClanName.trim() === '') {
            toast.error('Bitte einen Namen eingeben');
            return;
        }

        const existingClan = clans.find(clan => clan.name.toLowerCase() === newClanName.toLowerCase());
        if (existingClan) {
            toast.error('Dieser Clan-Name existiert bereits');
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/createClan?ClanName=${encodeURIComponent(newClanName)}`, {
                method: 'POST',
            });

            if (response.ok) {
                toast.success('Clan erfolgreich erstellt');
                await fetchClans(); 
            } else if (response.status === 409) { // Konflikt-Statuscode
                const errorMessage = await response.text();
                toast.error(errorMessage);
            } else {
                toast.error('Fehler beim Erstellen des Clans');
            }
        } catch (error) {
            console.error('There was a problem with the fetch operation:', error);
            toast.error('Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.');
        }
    };

    const joinClan = async (clanId, userId) => {
        try {
            const response = await fetch(`http://localhost:8080/joinClan?clanId=${clanId}&UserId=${userId}`, {
                method: 'POST',
            });

            const result = await response.text(); // Behandle die Antwort als Text
            if (response.ok) {
                if (result.includes("You have joined a clan")) {
                    setUserClanId(clanId);
                    toast.success("Du bist dem Clan erfolgreich beigetreten");
                } else if(response.status === 409) {
                    toast.error('Du bist bereits Mitglied dieses Clans'); 
                }
            } else {
                toast.error(result);
            }
        } catch (error) {
            console.error('There was a problem with the fetch operation:', error);
            toast.error('Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.');
        }
    };

    const leaveClan = async (userId) => {
        try {
            const response = await fetch(`http://localhost:8080/leaveClan?userId=${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                setUserClanId(null);
                toast.success('Erfolgreich den Clan verlassen');
                fetchClans(); // Aktualisiere die Liste der Clans
            } else {
                toast.error('Fehler beim Verlassen des Clans');
            }
        } catch (error) {
            console.error('There was a problem with the fetch operation:', error);
            toast.error('Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.');
        }
    };

    const handleCreateClan = () => {
        createClan();
    };

    const handleSelectClan = (clan) => {
        setSelectedClan(clan);
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setSelectedClan(null);
    };

    return (
        <div className="clan-list-container">
            <ToastContainer />
            <BackButton />
            <h1>Clans</h1>
            <input
                type="text"
                placeholder="Neuen Clan erstellen"
                value={newClanName}
                onChange={(e) => setNewClanName(e.target.value)}
            />
            <button onClick={handleCreateClan}>Clan erstellen</button>
            <div className="clan-list">
                {clans.map((clan) => (
                    <div
                        key={clan.id}
                        className={`clan-item ${userClanId === clan.id ? 'highlighted-clan' : ''}`}
                        onClick={() => handleSelectClan(clan)}
                    >
                        <span>{clan.name}</span>
                    </div>
                ))}
            </div>
            <Modal
                isOpen={showModal}
                onRequestClose={handleCloseModal}
                contentLabel="Clan Modal"
                className="clan-modal"
                overlayClassName="clan-modal-overlay"
            >
                {selectedClan && (
                    <ClanModal
                        clan={selectedClan}
                        onClose={handleCloseModal}
                        userId={userId}
                        userClanId={userClanId}
                        joinClan={joinClan}
                        leaveClan={leaveClan}
                    />
                )}
            </Modal>
        </div>
    );
}

export default Clan;
