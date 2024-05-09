import React from 'react';
import LoginPage from './components/Login/LoginPage';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import TwoFaktorAuthenfication from './components/2FA/TwoFactorAuthentication';
import Register from './components/Registrierung/Register.jsx' 
import Startseite from './components/Startseite/Startsite.jsx';
import DeckEditor from './components/Deckarbeiten/DeckEditor.jsx';
import AdminPanel from './components/Adminsteuerfeld/Admin.jsx';
import Profile from './components/Profilansicht/Profile.jsx';
import CreateDeck from './components/Deck-erstellen/CreateDeck.jsx';


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/registration" element={<Register/>} /> 
        <Route path="/startseite" element={<Startseite/>} /> {/* Die Namen der Seiten müssen groß geschrieben werden, damit das klappt*/}
        <Route path="/2fa" element={<TwoFaktorAuthenfication/>} />
        <Route path="/profil/:id" element={<Profile/>} /> 
        <Route path='/deckbearbeiten' element = {<DeckEditor/>} /> 
        <Route path='/admin' element = {<AdminPanel/>} /> 
        <Route path="/create-deck" element={<CreateDeck />} />
      </Routes>
    </Router>
  );
}
export default App;import React from 'react';
import LoginPage from './components/Login/LoginPage';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import TwoFaktorAuthenfication from './components/2FA/TwoFactorAuthentication';
import Profile from './components/Profilansicht/Profile.jsx';
import Register from './components/Registrierung/Register.jsx' 
import Startseite from './components/Startseite/Startsite.jsx';
import DeckEditor from './components/Deckarbeiten/DeckEditor.jsx';
import CreateDeck from './components/Deck-erstellen/CreateDeck.jsx';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/create-deck" element={<CreateDeck />} />
        <Route path="/register" element={<Register/>} /> 
        <Route path="/startseite" element={<Startseite/>} /> {/* Die Namen der Seiten müssen groß geschrieben werden, damit das klappt*/}
        <Route path="/2fa" element={<TwoFaktorAuthenfication/>} />
        <Route path="/profile/:id" element={<Profile/>} />
        <Route path='/deckbearbeiten' element = {<DeckEditor/>} /> 
      </Routes>
    </Router>
  );
}
export default App;