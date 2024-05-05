import React from 'react';
import LoginPage from './components/Login/LoginPage';
import RegisterPage from './components/RegisterPage';
import SuccesfulLog from './components/Login/SuccessfulLog';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import TwoFaktorAuthenfication from './components/2FA/TwoFaktorAuthenfication';
import Profile from './components/Profilansicht/Profile.jsx'; 


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/profileinsicht" element={<Profile/>}></Route>

        <Route path="/register" element={<RegisterPage />} />
        <Route path="/startseite" element={<SuccesfulLog/>} /> {/* Die Namen der Seiten müssen groß geschrieben werden, damit das klappt*/}
        <Route path="/2fa" element={<TwoFaktorAuthenfication/>} />
        <Route path="/profile" element={<Profile/>} />
      </Routes>
    </Router>
  );
}
export default App;