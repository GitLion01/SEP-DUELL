import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Axios from 'axios';
import './Profile.css';

// Profile-Seite

function Profile() {
  const [username, setUsername] = useState('');
  const [vorname, setVorname] = useState('');
  const [nachname, setNachname] = useState('');
  const [geburtsdatum, setDateOfBirth] = useState('');
  const [error, setError] = useState(null);

  useEffect(() => {
    Axios.get('http://localhost:8080/api/profile')
      .then(response => {
        const userData = response.data;
        setUsername(userData.username);
        setVorname(userData.vorname);
        setNachname(userData.nachname);
        setDateOfBirth(userData.geburtsdatum);
      })
      .catch(error => {
        console.error(error);
        setError('Ein Fehler ist aufgetreten')
      });
  }, []);

  return (
    <>
    <div className="Profile">
      <h1 className="titel"> Mein Profil</h1>
      <div className="daten">
      <p><strong>Username:</strong> {username}</p>
      <p><strong>Vorname:</strong> {vorname}</p>
      <p><strong>Nachname:</strong> {nachname}</p>
      <p><strong>Date of Birth:</strong> {geburtsdatum}</p>
      {error && <p className="error">{error}</p>} {/* Fehlermeldung anzeigen falls error != null */}
      </div>
    </div>
    <div>
      <Link to="/startseite">
        <button className="button" type="button">Home</button>
      </Link>
    </div>
    </>
  );
}

export default Profile;