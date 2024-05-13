import React, { useState, useEffect } from 'react';
import './Freundeliste.css';

function App() {
    // State für die Nutzerliste, die Freunde und die Freundschaftsanfragen
    const [users, setUsers] = useState([]);
    const [friends, setFriends] = useState([]);

    // Funktionen zum Laden der Daten vom Backend
    useEffect(() => {
        fetchUsers();
        fetchFriends();
        sendFriendRequest();
        //fetchFriendRequests(); // Die Methode fetchFriendRequests wurde auskommentiert, da sie nicht verwendet wird.
    }, []);

    const fetchUsers = async () => {
        try {
            const url = `http://localhost:8080/registration/users`;
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setUsers(data);
        } catch (error) {
            console.error('Fetch failed:', error.message);
        }
    };

    const fetchFriends = async () => {
        try {
            const userId = localStorage.getItem('id');
            const url = `http://localhost:8080/friendlist/${userId}`;
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            setFriends(data);
        } catch (error) {
            console.error('Fetch failed:', error.message);
        }
    };

    const sendFriendRequest = (friendId) => {

        const userId = localStorage.getItem('id');
        const url = 'http://localhost:8080/friendlist/add';
        const requestData = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                id: userId,
                friend_id: friendId
            })
        };

        fetch(url, requestData)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                console.log(`Freundschaftsanfrage an ${userId} gesendet`);
            })
            .catch(error => {
                console.error('There was a problem with the network request:', error);
            });
    };

    const removeFriend = (friend) => {

        console.log('Removing friend:', friend);

        const url = 'http://localhost:8080/friendlist/remove';
        const requestData = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                friend_id: friend
            })
        };

        fetch(url, requestData)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                console.log('Backend response:', data);
                const index = friends.findIndex(f => f.id === friend.id);
                const updatedFriends = [...friends.slice(0, index), ...friends.slice(index + 1)];
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