import React, { useState, useEffect } from 'react';
import './Freundeliste.css';

function App() {
    // State für die Nutzerliste, die Freunde und die Freundschaftsanfragen
    const [users, setUsers] = useState([]);
    const [friends, setFriends] = useState([]);
    const [friendRequests, setFriendRequests] = useState([]);

    // Funktionen zum Laden der Daten vom Backend
    useEffect(() => {

        fetchUsers();
        // Lade Nutzerliste vom Backend
        fetchFriends();
        // Lade Freunde vom Backend

        // Lade Freundschaftsanfragen vom Backend
        fetchFriendRequests();
    }, []);


    const fetchUsers= async (id) => {
        try {
            // Die URL mit der id zusammensetzen
            const url = `http://localhost:8080/registration/users`;
            // Daten vom Backend abrufen
            const response = await fetch(url);

            // Überprüfen, ob die Anfrage erfolgreich war
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            // Antwortdaten in das JSON-Format konvertieren
            const data = await response.json();

            // Mit den Daten arbeiten, z.B. setUsers aufrufen
            setUsers(data);
        } catch (error) {
            // Fehler behandeln, z.B. Fehlermeldung ausgeben
            console.error('Fetch failed:', error.message);
        }
    };

    const fetchFriends = async (id) => {
        try {
            // Die URL mit der id zusammensetzen
            const userId = localStorage.getItem('id');// Benutzer-ID aus dem LocalStorage abrufen

            const url = `http://localhost:8080/friendlist/${userId}`;
            // Daten vom Backend abrufen
            const response = await fetch(url);

            // Überprüfen, ob die Anfrage erfolgreich war
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            // Antwortdaten in das JSON-Format konvertieren
            const data = await response.json();

            // Mit den Daten arbeiten, z.B. setUsers aufrufen
            setFriends(data);
        } catch (error) {
            // Fehler behandeln, z.B. Fehlermeldung ausgeben
            console.error('Fetch failed:', error.message);
        }
    };


    const fetchFriendRequests = async () => {
        try {
            // Daten von 'http://localhost:8080/friendlist/request' abrufen
            const response = await fetch('http://localhost:8080/friendlist/request');

            // Überprüfen, ob die Anfrage erfolgreich war
            if (!response.ok) {
                throw new Error('Failed to fetch friend requests');
            }

            // Die Antwort in JSON-Format umwandeln
            const data = await response.json();

            // Hier kannst du die Daten verwenden oder setzen, wie du es benötigst
            // Zum Beispiel:
            // setFriendRequests(data);

            console.log('Friend Requests:', data);
        } catch (error) {
            console.error('Error fetching friend requests:', error.message);
        }
    };



    const sendFriendRequest = (user) => {
        const url = 'http://localhost:8080/friendlist/add';
        const requestData = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                user: user
            })
        };

        fetch(url, requestData)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                console.log(`Freundschaftsanfrage an ${user} gesendet`);
            })
            .catch(error => {
                console.error('There was a problem with the network request:', error);
            });
    };



    const removeFriend = (friend) => {
        const url = 'http://localhost:8080/friendlist/remove';
        const requestData = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                friend: friend
            })
        };

        fetch(url, requestData)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                // Wenn die Backend-Anfrage erfolgreich war, entferne den Freund auch aus der Frontend-Liste
                const updatedFriends = friends.filter(f => f.id !== friend.id);
                setFriends(updatedFriends);
                console.log(`${friend.username} wurde aus der Freundesliste entfernt`);
            })
            .catch(error => {
                console.error('There was a problem with the network request:', error);
            });
    };




    return (
        <div className="BFF">

            <div className="UserList-hülle">
                <h2>Nutzerliste</h2>
                <div className="UserList">
                    <ul>
                        {users.map(user => (
                            <li key={user.id}>
                                {user.username} {/* Oder eine andere Eigenschaft, die du anzeigen möchtest */}
                                <button onClick={() => sendFriendRequest(user)}>Add</button>
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
                                {friend.username}
                                <button onClick={() => removeFriend(friend)}>Freund entfernen</button>
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
        </div>
    );
}

export default App;