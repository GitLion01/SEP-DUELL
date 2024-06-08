import React, { useState } from 'react';
import './Startseite.css';
import { useNavigate } from 'react-router-dom';

const Startseite = () => {
  const [loggedIn, setLoggedIn] = useState(true);
  const navigate = useNavigate();

  const handleLogout = () => {
    setLoggedIn(false);
    localStorage.removeItem('id');
    localStorage.removeItem('userRole');
    navigate('/');
  };

  const handleAdminClick = (event) => {
    const userRole = localStorage.getItem('userRole');
    if (userRole !== 'ADMIN') {
      event.preventDefault(); // Verhindert die Weiterleitung
      alert('Zugriff verweigert! Nur Admins können auf das Adminsteuerfeld zugreifen.');
    }
  };

  if (!loggedIn) {
    console.log("Benutzer ausgeloggt, Weiterleitung zur Login-Seite");
    // Hier können Sie eine Weiterleitung zur Login-Seite implementieren
  }

  return (
    <div className="AppStart">
      <header>
        <h1>STARTSEITE</h1>
        <div className="logout-button">
          <button onClick={handleLogout}>Abmelden</button>
        </div>
      </header>
      <main>
        <div className="centered-content">
          <section className="spiel">
            <a href="/spielen"><h2>SPIEL START</h2></a>
          </section>
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
        </div>
      </main>
    </div>
  );
};

export default Startseite;
