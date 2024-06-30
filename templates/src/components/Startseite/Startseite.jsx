import React, { useState } from 'react';
import './Startseite.css';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const Startseite = () => {
  const [loggedIn, setLoggedIn] = useState(true);
  const navigate = useNavigate();

  const handleLogout = async () => {
    const userId = localStorage.getItem('id');
    if (userId) {
      await fetch(`http://localhost:8080/login/logout/${userId}`, { // Benutzer-ID im Pfad
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const socket = new SockJS('http://localhost:8080/game-websocket'); // WebSocket-Verbindung erstellen
      const stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
      });

      stompClient.onConnect = () => {
        const offline="offline";
        stompClient.publish({
          destination: '/status/status',
          body: JSON.stringify(offline),
          headers: {
            userId: userId.toString(),
          },
        });
        console.log('Disconnected: ');
        stompClient.deactivate();
      };

      stompClient.onStompError = (frame) => {
        console.error(`Broker reported error: ${frame.headers['message']}`);
        console.error(`Additional details: ${frame.body}`);
      };

      stompClient.activate();
    }

    setLoggedIn(false);
    localStorage.removeItem('id');
    localStorage.removeItem('userRole');
    navigate('/');
  };

  const handleAdminClick = (event) => {
    const userRole = localStorage.getItem('userRole');
    if (userRole !== 'ADMIN') {
      event.preventDefault();
      alert('Zugriff verweigert! Nur Admins können auf das Adminsteuerfeld zugreifen.');
    }
  };

  if (!loggedIn) {
    console.log("Benutzer ausgeloggt, Weiterleitung zur Login-Seite");
  }

  return (
      <div className="AppStart">
        <header>
          <h1>STARTSEITE</h1>
          <div className="logout-button">
            <button onClick={handleLogout}>Abmelden</button>
          </div>
          <div className="leaderboard-button">
            <button onClick={() => navigate('/leaderboard')}>Leaderboard</button>
          </div>
        </header>
        <main>
          <div className="centered-content">
            <section className="meinprofile">
              <a href="/profil"><h2>MEIN PROFIL</h2></a>
            </section>
            <section className="meindeck">
              <a href="/decks"><h2>MEIN DECK</h2></a>
            </section>
            <section className="freundesliste">
              <a href="/freundelist"> <h2>MEINE FREUNDESLISTE</h2></a>
            </section>
            <section className="adminsteuerfeld">
              <a href="/admin" onClick={handleAdminClick}><h2>MEIN ADMINSTEUERFELD</h2></a>
            </section>
            <section className="shop">
              <a href="/shop"><h2>SHOP</h2></a>
            </section>
            <section className="shop">
              <a href="/chat"><h2>CHAT</h2></a>
            </section>
            <section className="liveduelle">
              <a href="/streams"><h2>LIVE DUELLE</h2></a>
            </section>
            <section>
              <a href="/botdeckselect"><h2>BOT DUELL</h2></a>
            </section>
          </div>
        </main>
      </div>
  );
};

export default Startseite;
