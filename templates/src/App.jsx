import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import LoginPage from './components/Login/LoginPage';
import TwoFaktorAuthenfication from './components/2FA/TwoFactorAuthentication';
import Register from './components/Registrierung/Register.jsx';
import Startseite from './components/Startseite/Startseite.jsx';
import AdminPanel from './components/Adminsteuerfeld/Admin.jsx';
import Profile from './components/Profilansicht/Profile.jsx';
import Decks from './components/Deck-erstellen/Decks.jsx';
import Freundeliste from './components/Freunde/Freundeliste.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';
import ShopPage from './components/ShopPage/ShopPage.jsx';
import ChatPage from './components/chat/ChatPage.jsx';
import DeckSelection from './components/Duell/DeckSelection.jsx';
import Duel from './components/Duell/Duel.jsx';
import LeaderboardPage from './components/LeaderboardPage/LeaderboardPage.jsx';
import './index.css';
import DuelC from "./components/DuelC/DuelC";
import {WebSocketProvider} from "./WebSocketProvider";
import GlobalNotification from './components/LeaderboardPage/GlobalNotification.jsx';
import LiveTabelle from "./components/Livestream/LiveTabelle";
import Livestream from "./components/Livestream/Livestream";
import BotDeckSelect from "./components/Duell/BotDeckSelect";
import BotDuel from "./components/Duell/BotDuel";
import DuellHistorie from "./components/Profilansicht/DuellHistorie";

import Clan from "./components/clan/ClanList.jsx";
import TournamentPage from './components/Turnier/TournamentPage.jsx';
import GlobalTournamentNotification from './components/Turnier/GlobalTournamentNotification.jsx';

function App() {
  return (
    <Router>
      <WebSocketProvider>
        <GlobalNotification />
        <GlobalTournamentNotification />
        <AppRoutes />
      </WebSocketProvider>
    </Router>
  );
}

function AppRoutes() {
  const navigate = useNavigate();

  useEffect(() => {
    const handleGameCreated = (event) => {
      const gameId = event.detail;
      console.log(`Navigating to /deck-selection with gameId: ${gameId}`);
      navigate('/deck-selection');
    };

    window.addEventListener('gameCreated', handleGameCreated);

    return () => {
      window.removeEventListener('gameCreated', handleGameCreated);
    };
  }, [navigate]);

  return (
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/registration" element={<Register />} />
        <Route path="/startseite" element={<ProtectedRoute element={Startseite} />} />
        <Route path="/2fa" element={<TwoFaktorAuthenfication />} />
        <Route path="/profil" element={<ProtectedRoute element={Profile} />} />
        <Route path="/admin" element={<ProtectedRoute element={AdminPanel} requiredRole="ADMIN" />} />
        <Route path="/decks" element={<ProtectedRoute element={Decks} />} />
        <Route path="/freundelist" element={<ProtectedRoute element={Freundeliste} />} />
        <Route path="/shop" element={<ProtectedRoute element={ShopPage} />} />
        <Route path="/chat" element={<ProtectedRoute element={ChatPage} />} />
        <Route path="/leaderboard" element={<ProtectedRoute element={LeaderboardPage} />} />
        <Route path="/challenge-player" element={<ProtectedRoute element={DuelC} />} />
        <Route path="/deck-selection" element={<ProtectedRoute element={DeckSelection} />} />
        <Route path="/duel" element={<ProtectedRoute element={Duel} />} />
          <Route path="/clan" element={<ProtectedRoute element={Clan} />} />
          <Route path="/turnier" element={<ProtectedRoute element={TournamentPage}  />} />
          <Route path="/streams" element={<ProtectedRoute element={LiveTabelle} />} />
          <Route path="liveduel" element={<ProtectedRoute element={Livestream} /> } />
          <Route path="/botdeckselect" element={<ProtectedRoute element={BotDeckSelect} />} />
          <Route path="/botduel" element={<ProtectedRoute element={BotDuel} /> } />
          <Route path="/duellhistorie" element={<ProtectedRoute element={DuellHistorie} /> } />
      </Routes>
  );
}
export default App;