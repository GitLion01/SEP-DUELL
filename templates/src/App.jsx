import React from 'react';
import LoginPage from './components/Login/LoginPage';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import TwoFaktorAuthenfication from './components/2FA/TwoFactorAuthentication';
import Profile from './components/Profilansicht/Profile.jsx';
import Register from './components/Registrierung/Register.jsx' 
import Startseite from './components/Startseite/Startsite.jsx';
import DeckEditor from './components/Deckarbeiten/DeckEditor.jsx';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage/>} />
        <Route path="/registration" element={<Register/>} /> 
        <Route path="/startseite" element={<Startseite/>} /> {/* Die Namen der Seiten müssen groß geschrieben werden, damit das klappt*/}
        <Route path="/2fa" element={<TwoFaktorAuthenfication/>} />
        <Route path="/profile/:id" element={<Profile/>} />
        <Route path='/deckbearbeiten' element = {<DeckEditor/>} /> 
      </Routes>
    </Router>
  );
}
export default App;