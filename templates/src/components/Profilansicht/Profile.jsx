import React, { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import Axios from 'axios';
import './Profile.css';

// Profil-Seite

function Profile() {
  const id = localStorage.getItem('id');
  const [profilePicture, setProfilePicture] = useState('');
  const [username, setUsername] = useState('');
  const [vorname, setVorname] = useState('');
  const [nachname, setNachname] = useState('');
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('');
  const [geburtsdatum, setDateOfBirth] = useState('');
  const [sepCoins, setSepcoins] = useState('');
  const [leaderbordpunkte, setLeaderbordPunkte] = useState('');
  const [role, setRole] =useState('');
  const [error, setError] = useState(null);
  const [userdata, setUserdata] = useState('');
  
  // Benutzerdaten von der API abrufen
  useEffect(() => {
    Axios.get(`http://localhost:8080/profile/${id}`)
      .then(response => {
          console.log(response.data);
        const userData = response.data;
        setUserdata(response.data);
          setProfilePicture(userData.image);
        setUsername(userData.username);
        setVorname(userData.firstName);
        setNachname(userData.lastName);
        setEmail(userData.email);
        setPassword(userData.password);
        const date = new Date(userData.dateOfBirth);
        const formattedDate = `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getFullYear()}`;
        setDateOfBirth(formattedDate);
        setRole(userData.role);
          if (userData.role !== 'ADMIN') {
              setSepcoins(userData.sepCoins);
              setLeaderbordPunkte(userData.leaderboardPoints);
          }
      })
      .catch(error => {
        console.error(error);
        setError('Ein Fehler ist aufgetreten')
      });
  }, [id]);


  // Update SEP-Coins wenn der Wert von SEP-Coins geändert wird
  useEffect(() => {
    if (role !== 'ADMIN') {
      setSepcoins(userdata.sepCoins);
    }
  }, [role, userdata.sepCoins]);

  // Update Leaderboard-Punkte wenn der Wert von Leaderboard-Punkte geändert wird
  useEffect(() => {
    if (role !== 'ADMIN') {
        setLeaderbordPunkte(userdata.leaderboardPoints);
    }
  }, [role, userdata.leaderboardPoints])


  // Profilseite rendern
  return (
    <>
    <BackButton />
    <div className="Profile">
      <h1 className="titel"> Mein Profil</h1>
      <div className="daten">
        {/* Profilbild anzeigen und Ändern */}
        <input type="file" accept="image/*" onChange={(event) => {
        const file = event.target.files[0];
        const formData = new FormData();
        formData.append('file', file);
        Axios.post('http://localhost:8080/profile/image', formData)
        .then(response => {
            console.log('Profile picture uploaded successfully');
            setProfilePicture(response.data.profilePicture);
          })
        .catch(error => {
            console.error(error);
            setError('Ein Fehler ist aufgetreten');
          });
        }} style={{display: 'none'}} />
      <img className="profilbild" src={profilePicture ? `data:image/jpeg;base64,${profilePicture}` : require('./testbild.jpg') } alt={'Profilbild'} onClick={() => document.querySelector('input[type="file"]').click()} />
      <p><strong>Username:</strong> {username}</p>
      <p><strong>Vorname:</strong> {vorname}</p>
      <p><strong>Nachname:</strong> {nachname}</p>
      <p><strong>Email:</strong> {email}</p>
      {/*<p><strong>Passwort:</strong> {password} </p>*/}
      <p><strong>Date of Birth:</strong> {geburtsdatum}</p>
      {role !== 'ADMIN' && <p><strong>SEP Coins:</strong> {sepCoins}</p>}
      {role !== 'ADMIN' && <p><strong>Leaderboard Punkte:</strong> {leaderbordpunkte}</p>}
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