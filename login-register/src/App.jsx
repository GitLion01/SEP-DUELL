import React from 'react';
import LoginPage from './components/Login/LoginPage';
import RegisterPage from './components/RegisterPage';
import SuccesfulLog from './components/Login/SuccessfulLog';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/startseite" element={<SuccesfulLog/>} /> {/* Die Namen der Seiten müssen groß geschrieben werden, damit das klappt*/}
      </Routes>
    </Router>
  );
}

export default App;