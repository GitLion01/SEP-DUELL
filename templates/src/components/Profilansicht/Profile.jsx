import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Axios from 'axios';
import './Profile.css';

// Profil-Seite

function Profile() {
  let [profilePicture, setProfilePicture] = useState('');
  const [username, setUsername] = useState('');
  const [vorname, setVorname] = useState('');
  const [nachname, setNachname] = useState('');
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('');
  const [geburtsdatum, setDateOfBirth] = useState('');
  const [sepcoins, setSepcoins] = useState('');
  const [leaderbordpunkte, setLeaderboardPunkte] = useState('');
  const [role, setRole] =useState('');
  const [error, setError] = useState(null);
  let [userdata, setUserdata] = useState('');
  
  // Benutzerdaten von der API abrufen
  useEffect(() => {
    Axios.get('http://localhost:8080/api/profile')
      .then(response => {
        const userData = response.data;
        setUserdata(response.data);
        setProfilePicture(userData.profilePicture);
        setUsername(userData.username);
        setVorname(userData.vorname);
        setNachname(userData.nachname);
        setEmail(userData.email);
        setPassword(userData.password);
        setDateOfBirth(userData.geburtsdatum);
        setRole(userData.role);
        if (userData.role !== 'admin') {
          setSepcoins(userData.sepcoins);
          setLeaderboardPunkte(userData.leaderbordpunkte);
        };
      })
      .catch(error => {
        console.error(error);
        setError('Ein Fehler ist aufgetreten')
      });
  }, []);


  // Update SEP-Coins wenn der Wert von SEP-Coins geändert wird
  useEffect(() => {
    if (role !== 'admin') {
      setSepcoins(userdata.sepcoins);
    }
  }, [role, userdata.sepcoins]);

  // Update Leaderboard-Punkte wenn der Wert von Leaderboard-Punkte geändert wird
  useEffect(() => {
    if (role !== 'admin') {
      setLeaderboardPunkte(userdata.leaderbordpunkte);
    }
  }, [role, userdata.leaderbordpunkte])

  // Profilseite rendern
  return (
    <>
    <div className="Profile">
      <h1 className="titel"> Mein Profil</h1>
      <div className="daten">
        {/* Profilbild anzeigen und Ändern */}
        <input type="file" accept="image/*" onChange={(event) => {
        const file = event.target.files[0];
        const formData = new FormData();
        formData.append('file', file);
        Axios.post('http://localhost:8080/api/profile-picture', formData)
        .then(response => {
            console.log('Profile picture uploaded successfully');
            setProfilePicture(response.data.profilePicture);
          })
        .catch(error => {
            console.error(error);
            setError('Ein Fehler ist aufgetreten');
          });
        }} style={{display: 'none'}} />
      <img className="profilbild" src={profilePicture ? profilePicture : require('./testbild.jpg')} alt="Profilbild" onClick={() => document.querySelector('input[type="file"]').click()} />
      <p><strong>Username:</strong> {username}</p>
      <p><strong>Vorname:</strong> {vorname}</p>
      <p><strong>Nachname:</strong> {nachname}</p>
      <p><strong>Email:</strong> {email}</p>
      <p><strong>Passwort:</strong> {password} </p>
      <p><strong>Date of Birth:</strong> {geburtsdatum}</p>
      {role !== 'admin' && <p><strong>SEP Coins:</strong> {sepcoins}</p>}
      {role !== 'admin' && <p><strong>Leaderboard Punkte:</strong> {leaderbordpunkte}</p>}
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