import React from 'react';
import LoginPage from './components/Login/LoginPage';
import RegisterPage from './components/RegisterPage';
import SuccesfulLog from './components/Login/SuccessfulLog';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import TwoFaktorAuthenfication from './components/2FA/TwoFactorAuthentication';
import Profile from './components/Profileinsicht/Profile';
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/registration" element={<RegisterPage />} />
        <Route path="/startseite" element={<SuccesfulLog/>} /> {/* Die Namen der Seiten müssen groß geschrieben werden, damit das klappt*/}
        <Route path="/2fa" element={<TwoFaktorAuthenfication/>} />
        <Route path="/profile" element={<Profile/>} />
      </Routes>
    </Router>
  );
}
export default App;