import React, { useEffect, useState, useCallback } from 'react';
import Modal from 'react-modal';
import './Freundeliste.css';
import '../ProfileForFriends/ProfileFriends.css';
import BackButton from '../BackButton';

// Setzt das Root-Element für die Zugänglichkeit des Modals
Modal.setAppElement('#root');

function App() {
    const [users, setUsers] = useState([]); // Zustand für alle Nutzer
    const [friends, setFriends] = useState([]); // Zustand für Freundesliste
    const [selectedFriend, setSelectedFriend] = useState(null); // Zustand für ausgewählten Freund
    const [selectedFriendFriends, setSelectedFriendFriends] = useState([]); // Zustand für Freunde des ausgewählten Freundes
    const [modalIsOpen, setModalIsOpen] = useState(false); // Zustand für Modal-Öffnung
    const [isChecked, setIsChecked] = useState(false); // Zustand für Checkbox (Privatsphäre-Einstellung)
    const userId = localStorage.getItem('id'); // Holt die Benutzer-ID aus dem lokalen Speicher

    // Funktion zum Abrufen aller Nutzer vom Server
    const fetchUsers = async () => {
        const url = 'http://localhost:8080/registration/users';
        const response = await fetch(url);
        if (!response.ok) throw new Error('Network response was not ok');
        return await response.json();
    };

    // Funktion zum Abrufen der Freundesliste des aktuellen Nutzers vom Server
    const fetchFriends = useCallback(async () => {
        const url = `http://localhost:8080/friendlist/${userId}`;
        const response = await fetch(url);
        if (!response.ok) throw new Error('Network response was not ok');
        return await response.json();
    }, [userId]);

    // Funktion zum Abrufen der Privatsphäre-Einstellung der Freundesliste des aktuellen Nutzers
    const setCheckBox = useCallback(() => {
        const url = `http://localhost:8080/friendlist/getFriendslistPrivacy?userId=${userId}`;
        fetch(url, { method: 'GET' })
            .then(response => response.json())
            .then(data => {
                setIsChecked(data);
            })
            .catch(error => {
                console.error('Error:', error);
            });
    }, [userId]);

    // useEffect Hook, der beim ersten Rendern und bei Änderungen von userId, fetchFriends, und setCheckBox aufgerufen wird
    useEffect(() => {
        const fetchData = async () => {
            try {
                const [friendsResponse, usersResponse] = await Promise.all([fetchFriends(), fetchUsers()]);
                setFriends(friendsResponse); // Setzt die Freundesliste

                // Filtert die Nutzerliste, um Nutzer auszuschließen, die bereits Freunde sind oder der aktuelle Nutzer selbst sind
                const filteredUsers = usersResponse.filter(
                    user => user.id.toString() !== userId.toString() &&
                        !friendsResponse.some(friend => friend.id.toString() === user.id.toString())
                );
                setUsers(filteredUsers); // Setzt die Nutzerliste
            } catch (error) {
                console.error('Fetch failed:', error.message);
            }
        };

        fetchData(); // Ruft die Daten ab
        setCheckBox(); // Setzt den Zustand der Checkbox
    }, [userId, fetchFriends, setCheckBox]);

    // Funktion zum Senden einer Freundschaftsanfrage an einen anderen Nutzer
    const sendFriendRequest = (friendId) => {
        const url = 'http://localhost:8080/friendlist/add';
        const requestData = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify([userId, friendId])
        };

        fetch(url, requestData)
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                console.log(`Freundschaftsanfrage an ${friendId} gesendet`);
                alert('Freundschaftsanfrage wurde gesendet');
            })
            .catch(error => {
                console.error('There was a problem with the network request:', error);
            });
    };

    // Funktion zum Abrufen der Freundesliste eines Freundes des aktuellen Nutzers
    const fetchFriendsOfFriend = async (userId, friendId) => {
        try {
            const url = `http://localhost:8080/friendlist/${userId}/friends/${friendId}`;
            const response = await fetch(url);
            if (!response.ok) {
                if (response.status === 403) {
                    console.warn('Zugriff auf die Freundesliste dieses Freundes ist nicht erlaubt.');
                    setSelectedFriendFriends([]);
                    return;
                }
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setSelectedFriendFriends(data); // Setzt die Freunde des ausgewählten Freundes
        } catch (error) {
            console.error('Fetch failed:', error.message);
            setSelectedFriendFriends([]);
        }
    };

    // Funktion zum Entfernen eines Freundes aus der Freundesliste des aktuellen Nutzers
    const removeFriend = (friendId) => {
        const url = 'http://localhost:8080/friendlist/remove';
        const requestData = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify([userId, friendId])
        };

        fetch(url, requestData)
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(data => {
                console.log('Backend response:', data);
                const updatedFriends = friends.filter(f => f.id !== friendId); // Aktualisiert die Freundesliste
                setFriends(updatedFriends); // Setzt die aktualisierte Freundesliste
                console.log(`Freund mit ID ${friendId} wurde aus der Freundesliste entfernt`);
                alert('Freund wurde aus der Freundesliste entfernt');
            })
            .catch(error => {
                console.error('There was a problem with the network request:', error);
            });
    };

    // Funktion zum Öffnen des Modals und Abrufen der Freundesliste des ausgewählten Freundes
    const openModal = (friend) => {
        setSelectedFriend(friend); // Setzt den ausgewählten Freund
        fetchFriendsOfFriend(userId, friend.id); // Ruft die Freundesliste des Freundes ab
        setModalIsOpen(true); // Öffnet das Modal
    };

    // Funktion zum Schließen des Modals und Zurücksetzen des Zustands
    const closeModal = () => {
        setSelectedFriend(null); // Setzt den ausgewählten Freund zurück
        setModalIsOpen(false); // Schließt das Modal
        setSelectedFriendFriends([]); // Setzt die Freunde des ausgewählten Freundes zurück
    };

    // Funktion zum Ändern der Privatsphäre-Einstellung der Freundesliste
    const handleCheckboxChange = (event) => {
        const url = `http://localhost:8080/friendlist/setFriendslistPrivacy?userId=${userId}`;
        const newIsChecked = event.target.checked;
        setIsChecked(newIsChecked); // Setzt den neuen Zustand der Checkbox

        fetch(url, { method: 'PUT' })
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                console.log('Privacy setting updated');
            })
            .catch(error => {
                console.error('Error:', error);
            });
    };

    return (
        <div className="BFF">
            <BackButton /> {/* Komponente für einen Zurück-Button */}
            <div className="UserList-hülle">
                <h2>Nutzerliste</h2>
                <div className="UserList">
                    <ul>
                        {users.map(user => (
                            <li key={user.id}>
                                {user.username}
                                <button onClick={() => sendFriendRequest(user.id)}>Add</button>
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
            <div className="FriendList-hülle">
                <h2>Meine Freunde</h2>
                <div className="FriendList">
                    <ul>
                        {friends.map(friend => (
                            <li key={friend.id}>
                                <span onClick={() => openModal(friend)} style={{ cursor: 'pointer', color: 'blue' }}>
                                    {friend.username}
                                </span>
                                <button onClick={() => removeFriend(friend.id)}>Freund entfernen</button>
                            </li>
                        ))}
                    </ul>
                </div>
                <div className="form-check form-switch">
                    <input
                        className="form-check-input"
                        type="checkbox"
                        role="switch"
                        id="flexSwitchCheckDefault"
                        onChange={handleCheckboxChange}
                        checked={isChecked}
                    />
                    <label className="form-check-label" htmlFor="flexSwitchCheckDefault">
                        Privat
                    </label>
                </div>
            </div>

            <Modal
                isOpen={modalIsOpen}
                onRequestClose={closeModal}
                contentLabel="Friend Profile"
                className="Modal"
                overlayClassName="Overlay"
            >
                {selectedFriend && (
                    <div className="profile-container">
                        <div className="profile-header">
                            <h2>Profil von {selectedFriend.username}</h2>
                        </div>
                        <div className="profile-info">
                            <img
                                className="profile-picture"
                                src={`data:image/jpeg;base64,${selectedFriend.image}`}
                                alt="Profilbild"
                            />
                            <p><strong>Benutzername:</strong> {selectedFriend.username}</p>
                            <p><strong>Vorname:</strong> {selectedFriend.firstName}</p>
                            <p><strong>Nachname:</strong> {selectedFriend.lastName}</p>
                            <p><strong>Leaderboard-Punkte:</strong> {selectedFriend.leaderboardPoints}</p>
                        </div>
                        <div className="friends-of-friend">
                            <h4>Freunde von {selectedFriend.username}</h4>
                            <div className="friends-list">
                                <ul>
                                    {selectedFriendFriends.map(friend => (
                                        <li key={friend.id}>{friend.username}</li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                        <button onClick={closeModal}>Schließen</button>
                    </div>
                )}
            </Modal>
        </div>
    );
}

export default App;
