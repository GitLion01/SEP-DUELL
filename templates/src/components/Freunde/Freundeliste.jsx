import React, { useState, useEffect } from 'react';
import './Freundeliste.css';
 
function App() {
  // State für die Nutzerliste, die Freunde und die Freundschaftsanfragen
  const [users, setUsers] = useState([]);
  const [friends, setFriends] = useState([]);
  const [friendRequests, setFriendRequests] = useState([]);
 
  // Funktionen zum Laden der Daten vom Backend
  useEffect(() => {
    // Lade Nutzerliste vom Backend
    fetchUsers();
    // Lade Freunde vom Backend
    fetchFriends();
    // Lade Freundschaftsanfragen vom Backend
    fetchFriendRequests();
  }, []);
 
 
 
  const fetchUsers = async (id) => {
    try {
        const id = 2; 
      // Die URL mit der id zusammensetzen
      const url = `http://localhost:8080/friendlist/${id}`;
     
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
 
  const fetchFriends = async () => {
    try {
      // Daten von 'http://localhost:8080/friendlist' abrufen, 
      const response = await fetch('http://localhost:8080/friendlist');
 
      // Überprüfen, ob die Anfrage erfolgreich war
      if (!response.ok) {
        throw new Error('Failed to fetch friends');
      }
 
      // Die Antwort in JSON-Format umwandeln
      const data = await response.json();
 
      // Hier kannst du die Daten verwenden oder setzen, wie du es benötigst
      // Zum Beispiel:
      // setFriends(data);
 
      console.log('Friends:', data);
    } catch (error) {
      console.error('Error fetching friends:', error.message);
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
 
 
  // Funktionen zum Senden von Freundschaftsanfragen
  const sendFriendRequest = (user) => {
    // Funktion zum Senden von Freundschaftsanfragen an das Backend
    console.log(`Freundschaftsanfrage an ${user} gesendet`);
  };
 
  // Funktionen zum Entfernen von Freunden
  const removeFriend = (friend) => {
    // Funktion zum Entfernen von Freunden aus der Freundesliste im Backend
    console.log(`${friend} wurde aus der Freundesliste entfernt`);
  };
 
 
 
 
  return (
    <div className="BFF">
      <div className="UserList">
        <h2>Nutzerliste</h2>
        <ul>
          {users.map(user => (
            <li key={user}>
              {user}
              <button onClick={() => sendFriendRequest(user)}>Add</button>
            </li>
          ))}
        </ul>
      </div>
      <div className="FriendList">
        <h2>Meine Freunde</h2>
        <ul>
          {friends.map(friend => (
            <li key={friend}>
              {friend}
              <button onClick={() => removeFriend(friend)}>Freund entfernen</button>
            </li>
          ))}
        </ul>
      </div>
     
    </div>
  );
}
 
export default App;