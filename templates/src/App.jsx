import React from 'react';
import LoginPage from './components/Login/LoginPage';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import TwoFaktorAuthenfication from './components/2FA/TwoFactorAuthentication';
import Register from './components/Registrierung/Register.jsx' 
import Startseite from './components/Startseite/Startseite.jsx';
import AdminPanel from './components/Adminsteuerfeld/Admin.jsx';
import Profile from './components/Profilansicht/Profile.jsx';
import Decks from './components/Deck-erstellen/Decks.jsx';
import './index.css';
import Freundeliste from "./components/Freunde/Freundeliste.jsx";
import ProtectedRoute from './ProtectedRoute.jsx';
import ShopPage from './components/ShopPage/ShopPage.jsx';
import ChatPage from './components/chat/ChatPage.jsx';




function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/registration" element={<Register/>} /> 
        <Route path="/startseite" element={<ProtectedRoute element={Startseite} />} /> {/* Die Namen der Seiten müssen groß geschrieben werden, damit das klappt*/}
        <Route path="/2fa" element={<TwoFaktorAuthenfication/>} />
        <Route path="/profil" element={<ProtectedRoute element={Profile} />} />
        <Route path="/admin" element={<ProtectedRoute element={AdminPanel} requiredRole="ADMIN" />} />
        <Route path="/decks" element={<ProtectedRoute element={Decks} />} />
        <Route path="/freundelist" element={<ProtectedRoute element={Freundeliste} />} />
        <Route path="/shop" element={<ProtectedRoute element={ShopPage} />} />
        <Route path="/chat" element={<ProtectedRoute element={ChatPage} />} /> {/* Neue Route für die Chat-Seite */}
      </Routes>
    </Router>
  );
}
export default App;