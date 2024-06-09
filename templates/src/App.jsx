import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import LoginPage from './components/Login/LoginPage';
import TwoFaktorAuthenfication from './components/2FA/TwoFactorAuthentication';
import Register from './components/Registrierung/Register.jsx';
import Startseite from './components/Startseite/Startseite.jsx';
import AdminPanel from './components/Adminsteuerfeld/Admin.jsx';
import Profile from './components/Profilansicht/Profile.jsx';
import Decks from './components/Deck-erstellen/Decks.jsx';
import Freundeliste from "./components/Freunde/Freundeliste.jsx";
import ProtectedRoute from './ProtectedRoute.jsx';
import ShopPage from './components/ShopPage/ShopPage.jsx';
import ChatPage from './components/chat/ChatPage.jsx';
import DeckSelection from './components/Duell/DeckSelection.jsx';
import Duel from "./components/Duell/Duel.jsx";

import './index.css';
import DuelC from "./components/DuelC/DuelC";


function App() {
  const [client, setClient] = useState(null);

  useEffect(() => {
    const newClient = new Client({
      brokerURL: 'ws://localhost:8080/game-websocket',
      webSocketFactory: () => new SockJS('http://localhost:8080/game-websocket'),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('Connected to WebSocket server');
      },
      onStompError: (frame) => {
        console.error(`Broker reported error: ${frame.headers['message']}`);
        console.error(`Additional details: ${frame.body}`);
      },
      onWebSocketError: (event) => {
        console.error('WebSocket error', event);
      },
      onWebSocketClose: (event) => {
        console.error('WebSocket closed', event);
      },
    });

    newClient.activate();
    setClient(newClient);

  }, []);

  if (!client) {
    return <div>Loading...</div>;
  }

  return (
      <Router>
        <Routes>
          <Route path="/" element={<LoginPage/>}/>
          <Route path="/registration" element={<Register/>}/>
          <Route path="/startseite" element={<ProtectedRoute
              element={Startseite}/>}/> {/* Die Namen der Seiten müssen groß geschrieben werden, damit das klappt*/}
          <Route path="/2fa" element={<TwoFaktorAuthenfication/>}/>
          <Route path="/profil" element={<ProtectedRoute element={Profile}/>}/>
          <Route path="/admin" element={<ProtectedRoute element={AdminPanel} requiredRole="ADMIN"/>}/>
          <Route path="/decks" element={<ProtectedRoute element={Decks}/>}/>
          <Route path="/freundelist" element={<ProtectedRoute element={Freundeliste}/>}/>
          <Route path="/shop" element={<ProtectedRoute element={ShopPage}/>}/>
          <Route path="/chat" element={<ProtectedRoute element={ChatPage}/>}/> {/* Neue Route für die Chat-Seite */}
          <Route path="/challenge-player" element={<ProtectedRoute element={() => <DuelC client={client} />} />} />

          {/* Neue Routen für das Duell */}

          <Route path="/deck-selection" element={<ProtectedRoute element={() => <DeckSelection client={client} />} />} />
          <Route path="/duel" element={<ProtectedRoute element={() => <Duel client={client} />} />} />

        </Routes>
      </Router>
  );
}
export default App;